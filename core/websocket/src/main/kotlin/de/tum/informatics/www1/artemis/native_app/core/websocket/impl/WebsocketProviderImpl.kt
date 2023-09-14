package de.tum.informatics.www1.artemis.native_app.core.websocket.impl

import android.net.Uri
import android.util.Log
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.JsonProvider
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.device.awaitInternetConnection
import de.tum.informatics.www1.artemis.native_app.core.websocket.WebsocketProvider
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompReceipt
import org.hildan.krossbow.stomp.config.HeartBeat
import org.hildan.krossbow.stomp.conversions.kxserialization.StompSessionWithKxSerialization
import org.hildan.krossbow.stomp.conversions.kxserialization.json.withJsonConversions
import org.hildan.krossbow.stomp.headers.StompSendHeaders
import org.hildan.krossbow.stomp.headers.StompSubscribeHeaders
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@OptIn(DelicateCoroutinesApi::class)
class WebsocketProviderImpl(
    serverConfigurationService: ServerConfigurationService,
    accountService: AccountService,
    private val jsonProvider: JsonProvider,
    private val networkStatusProvider: NetworkStatusProvider,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    allowReconnects: Boolean = true
) : WebsocketProvider {

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
            heartBeat = HeartBeat(60.seconds, 60.seconds)
            gracefulDisconnect = false
            connectionTimeout = 20.minutes
        }

    /**
     * Connects a stomp session only if it is actually needed.
     * After 10 seconds of not having any subscribers, the session will be closes
     */
    private val session: Flow<StompSessionWithKxSerialization> =
        combine(
            serverConfigurationService.serverUrl,
            serverConfigurationService.host,
            onWebsocketError.filter { allowReconnects }.onStart { emit(Unit) },
            onRequestReconnect.filter { allowReconnects }.onStart { emit(Unit) }
        ) { a, b, _, _ -> a to b }
            .transformLatest { (serverUrl, host) ->
                emitAll(
                    channelFlow {
                        Log.d(TAG, "Websocket: Init")

                        val uri = Uri.parse(serverUrl)

                        val protocol = if (uri.scheme == "http") URLProtocol.WS else URLProtocol.WSS
                        val port = when {
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
                            if (e !is CancellationException && allowReconnects) {
                                Log.d(TAG, "Websocket connection failure (attempt $attempt)", e)
                                // Either we wait the specified time, or we immediately try again when we have internet
                                withTimeoutOrNull(1.seconds * attempt.toInt()) {
                                    networkStatusProvider.awaitInternetConnection()
                                }
                                kotlinx.coroutines.delay(1.seconds * attempt.toInt())
                                true
                            } else false
                        }
                )
            }
            .shareIn(
                scope = GlobalScope + coroutineContext, // We share this is the app container
                started = SharingStarted.WhileSubscribed(
                    stopTimeout = 1.seconds, replayExpiration = Duration.ZERO
                ),
                replay = 1
            )

    override val connectionState: Flow<WebsocketProvider.WebsocketConnectionState> =
        session.transformLatest<StompSessionWithKxSerialization, WebsocketProvider.WebsocketConnectionState> { _ ->
            emit(WebsocketProvider.WebsocketConnectionState.WithSession(true))

            // Wait for error or reconnect
            merge(onWebsocketError, onRequestReconnect).first()

            // After error, we emit false, then wait for a new session.
            emit(WebsocketProvider.WebsocketConnectionState.WithSession(false))
        }
            .onStart { emit(WebsocketProvider.WebsocketConnectionState.Empty) }
            .shareIn(
                scope = GlobalScope + coroutineContext,
                started = SharingStarted.WhileSubscribed(),
                replay = 1
            )

    override val isConnected: Flow<Boolean> = connectionState
        .map { it.isConnected }
        .shareIn(
            scope = GlobalScope + coroutineContext,
            started = SharingStarted.WhileSubscribed(),
            replay = 1
        )

    override suspend fun <T> convertAndSend(
        headers: StompSendHeaders,
        body: T,
        serializer: SerializationStrategy<T>
    ): StompReceipt? = session.first().convertAndSend(headers, body, serializer)

    /**
     * Returns a flow that automatically unsubscribes once the collector is inactive.
     * The given flow can only be subscribed to once.
     */
    override fun <T : Any> subscribe(
        channel: String,
        deserializer: DeserializationStrategy<T>
    ): Flow<WebsocketProvider.WebsocketData<T>> {
        return session
            .transformLatest { currentSession ->
                val flow: Flow<WebsocketProvider.WebsocketData<T>> = flow {
                    emitAll(
                        currentSession.subscribe(
                            StompSubscribeHeaders(destination = channel),
                            deserializer
                        )
                    )
                }
                    .onStart {
                        Log.d(TAG, "subscribe! $channel")
                        emit(WebsocketProvider.WebsocketData.Subscribe())
                    }
                    .onCompletion {
                        Log.d(TAG, "unsubscribe! $channel")
                    }
                    .catch { e ->
                        Log.d(TAG, "Subscription $channel reported error: ${e.localizedMessage}")
                    }
                    .map {
                        WebsocketProvider.WebsocketData.Message(it)
                    }

                emitAll(
                    merge(
                        flow,
                        connectionState
                            .mapNotNull { currentState ->
                                if (currentState is WebsocketProvider.WebsocketConnectionState.WithSession && !currentState.isConnected) {
                                    WebsocketProvider.WebsocketData.Disconnect()
                                } else null
                            }
                    )
                        // Stop emitting on this flow if an error occurred and wait for a new session
                        .transformWhile { websocketData ->
                            emit(websocketData)

                            websocketData !is WebsocketProvider.WebsocketData.Disconnect
                        }
                )
            }
    }

    override fun <T : Any> subscribeMessage(
        channel: String,
        deserializer: DeserializationStrategy<T>
    ): Flow<T> {
        return subscribe(channel, deserializer).mapNotNull {
            when (it) {
                is WebsocketProvider.WebsocketData.Message -> it.message
                else -> null
            }
        }
    }

    override fun requestTryReconnect() {
        onRequestReconnect.tryEmit(Unit)
    }
}

