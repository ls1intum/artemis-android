package de.tum.informatics.www1.artemis.native_app.core.websocket.impl

import android.net.Uri
import android.util.Log
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.JsonProvider
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.device.awaitInternetConnection
import de.tum.informatics.www1.artemis.native_app.core.websocket.impl.WebsocketProvider.WebsocketData.Message
import io.ktor.http.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.DeserializationStrategy
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.config.HeartBeat
import org.hildan.krossbow.stomp.conversions.kxserialization.*
import org.hildan.krossbow.stomp.conversions.kxserialization.json.withJsonConversions
import org.hildan.krossbow.stomp.headers.StompSubscribeHeaders
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class WebsocketProvider(
    serverConfigurationService: ServerConfigurationService,
    accountService: AccountService,
    private val jsonProvider: JsonProvider,
    private val networkStatusProvider: NetworkStatusProvider
) {

    companion object {
        private const val TAG = "WebsocketProvider"
    }

    private val onRequestReconnect = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val onWebsocketError = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val onReconnected = MutableSharedFlow<Unit>()

    private val webSocketClient =
        CustomOkHttpWebSocketClient(
            authTokenFlow = accountService.authToken,
            onError = {
                Log.d(TAG, "Websocket on error. Emitting to try reconnect")
                val hasEmitted = onWebsocketError.tryEmit(Unit)
                Log.d(TAG, "Websocket closed. Emitted try reconnect=$hasEmitted")
            }
        )

    private val client =
        StompClient(webSocketClient) {
            heartBeat = HeartBeat(10.seconds, 10.seconds)
            gracefulDisconnect = false
            connectionTimeout = 20.minutes
        }

    /**
     * Connects a stomp session only if it is actually needed.
     * After 10 seconds of not having any subscribers, the session will be closes
     */
    @OptIn(DelicateCoroutinesApi::class)
    val session: Flow<StompSessionWithKxSerialization> =
        combine(
            serverConfigurationService.serverUrl,
            serverConfigurationService.host,
            onWebsocketError.onStart { emit(Unit) },
            onRequestReconnect.onStart { emit(Unit) }
        ) { a, b, _, _ -> a to b }
            .transformLatest { (serverUrl, host) ->
                emitAll(
                    channelFlow {
                        Log.d(TAG, "Websocket: Init")

                        val uri = Uri.parse(serverUrl)

                        val protocol = if (uri.scheme == "http") URLProtocol.WS else URLProtocol.WSS
                        val port = when  {
                            uri.port == -1 && protocol == URLProtocol.WS -> 80
                            uri.port == -1 && protocol == URLProtocol.WSS -> 443
                            else -> uri.port
                        }

                        val url = URLBuilder(
                            protocol = protocol,
                            host = host,
                            port = port,
                            pathSegments = listOf("websocket", "websocket")
                        ).buildString()

                        val session = client
                            .connect(url = url)
                            .withJsonConversions(jsonProvider.applicationJsonConfiguration)

                        send(session)

                        onReconnected.emit(Unit)
                        Log.d(TAG, "Websocket: Connected")

                        awaitClose {
                            Log.d(TAG, "Websocket: Stopped")

                            //Graceful disconnect is disabled, runBlocking should not block
                            runBlocking {
                                withTimeoutOrNull(200.milliseconds) {
                                    session.disconnect()
                                }
                            }
                        }
                    }
                        .retryWhen { e, attempt ->
                            // Never retry on cancellation
                            if (e !is CancellationException) {
                                Log.d(TAG, "Websocket connection failure (attempt $attempt)", e)
                                // Either we wait the specified time, or we immediately try again when we have internet
                                withTimeoutOrNull(1.seconds * attempt.toInt()) {
                                    networkStatusProvider.awaitInternetConnection()
                                }
                                delay(1.seconds * attempt.toInt())
                                true
                            } else false
                        }
                )
            }
            .shareIn(
                scope = GlobalScope, // We share this is the app container
                started = SharingStarted.WhileSubscribed(
                    stopTimeout = 1.seconds, replayExpiration = Duration.ZERO
                ),
                replay = 1
            )

    @OptIn(DelicateCoroutinesApi::class)
    val connectionState: Flow<WebsocketConnectionState> =
        session.transformLatest<StompSessionWithKxSerialization, WebsocketConnectionState> { _ ->
            emit(WebsocketConnectionState.WithSession(true))

            // Wait for error or reconnect
            merge(onWebsocketError, onRequestReconnect).first()

            // After error, we emit false, then wait for a new session.
            emit(WebsocketConnectionState.WithSession(false))
        }
            .onStart { emit(WebsocketConnectionState.Empty) }
            .shareIn(
                scope = GlobalScope,
                started = SharingStarted.WhileSubscribed(),
                replay = 1
            )

    val isConnected: Flow<Boolean> = connectionState.map { it.isConnected }

    /**
     * Returns a flow that automatically unsubscribes once the collector is inactive.
     * The given flow can only be subscribed to once.
     */
    fun <T : Any> subscribe(
        channel: String,
        deserializer: DeserializationStrategy<T>
    ): Flow<WebsocketData<T>> {
        return session
            .transformLatest { currentSession ->
                val flow: Flow<WebsocketData<T>> = flow {
                    emitAll(
                        currentSession.subscribe(
                            StompSubscribeHeaders(destination = channel),
                            deserializer
                        )
                    )
                }
                    .onStart {
                        Log.d(TAG, "subscribe! $channel")
                        emit(WebsocketData.Subscribe())
                    }
                    .onCompletion {
                        Log.d(TAG, "unsubscribe! $channel")
                    }
                    .catch { e ->
                        Log.d(TAG, "Subscription $channel reported error: ${e.localizedMessage}")
                    }
                    .map { Message(it) }

                emitAll(
                    merge(
                        flow,
                        connectionState
                            .mapNotNull { currentState ->
                                if (currentState is WebsocketConnectionState.WithSession && !currentState.isConnected) {
                                    WebsocketData.Disconnect()
                                } else null
                            }
                    )
                        // Stop emitting on this flow if an error occurred and wait for a new session
                        .transformWhile { websocketData ->
                            emit(websocketData)

                            websocketData !is WebsocketData.Disconnect
                        }
                )
            }
    }

    fun <T : Any> subscribeMessage(
        channel: String,
        deserializer: DeserializationStrategy<T>
    ): Flow<T> {
        return subscribe(channel, deserializer).mapNotNull {
            when (it) {
                is Message -> it.message
                else -> null
            }
        }
    }

    fun requestTryReconnect() {
        onRequestReconnect.tryEmit(Unit)
    }

    sealed interface WebsocketData<T> {
        class Subscribe<T> : WebsocketData<T>

        class Disconnect<T> : WebsocketData<T>

        class Message<T>(val message: T) : WebsocketData<T>
    }

    sealed interface WebsocketConnectionState {
        val isConnected: Boolean

        object Empty : WebsocketConnectionState {
            override val isConnected: Boolean = false
        }

        data class WithSession(override val isConnected: Boolean) : WebsocketConnectionState
    }
}

