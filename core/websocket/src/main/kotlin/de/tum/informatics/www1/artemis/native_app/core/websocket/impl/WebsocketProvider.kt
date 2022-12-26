package de.tum.informatics.www1.artemis.native_app.core.websocket.impl

import android.util.Log
import de.tum.informatics.www1.artemis.native_app.core.common.withPrevious
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.JsonProvider
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.device.awaitInternetConnection
import de.tum.informatics.www1.artemis.native_app.core.websocket.impl.WebsocketProvider.WebsocketData.Message
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
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import org.hildan.krossbow.stomp.conversions.kxserialization.*
import org.hildan.krossbow.stomp.conversions.kxserialization.json.withJsonConversions
import org.hildan.krossbow.stomp.headers.StompSubscribeHeaders
import kotlin.time.Duration.Companion.minutes

class WebsocketProvider(
    serverConfigurationService: ServerConfigurationService,
    accountService: AccountService,
    private val jsonProvider: JsonProvider,
    private val networkStatusProvider: NetworkStatusProvider
) {

    companion object {
        private const val TAG = "WebsocketProvider"
    }

    private val onWebsocketError = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val onReconnected = MutableSharedFlow<Unit>()

    private val webSocketClient =
        CustomOkHttpWebSocketClient(
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
            serverConfigurationService.host,
            accountService.authenticationData,
            onWebsocketError.onStart { emit(Unit) }
        ) { a, b, _ -> a to b }
            .transformLatest { (host, authenticationData) ->
                emitAll(
                    channelFlow {
                        Log.d(TAG, "Websocket: Init")
                        val url =
                            "wss://$host/websocket/tracker/websocket" + when (authenticationData) {
                                is AccountService.AuthenticationData.LoggedIn -> {
                                    "?access_token=${authenticationData.authToken}"
                                }

                                AccountService.AuthenticationData.NotLoggedIn -> ""
                            }

                        val session = client
                            .connect(url)
                            .withJsonConversions(jsonProvider.networkJsonConfiguration)

                        send(session)

                        onReconnected.emit(Unit)
                        Log.d(TAG, "Websocket: Connected")

                        awaitClose {
                            Log.d(TAG, "Websocket: Stopped")

                            //Graceful disconnect is disabled, runBlocking should not block
                            runBlocking {
                                session.disconnect()
                            }
                        }
                    }
                        .retryWhen { e, attempt ->
                            // Never retry on cancellation
                            if (e !is CancellationException) {
                                Log.d(TAG, "Websocket connection failure (attempt $attempt): $e.")
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
                scope = GlobalScope, //We share this is the app container
                started = SharingStarted.WhileSubscribed(
                    stopTimeout = 1.seconds, replayExpiration = Duration.ZERO
                ),
                replay = 1
            )

    @OptIn(DelicateCoroutinesApi::class)
    val isConnected: Flow<Boolean> =
        session.transformLatest { _ ->
            emit(true)

            // Wait for error
            onWebsocketError.first()

            // After error, we emit false, then wait for a new session.
            emit(false)
        }
            .onStart { emit(false) }
            .shareIn(
                scope = GlobalScope,
                started = SharingStarted.WhileSubscribed(),
                replay = 1
            )

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
                        isConnected.withPrevious().mapNotNull { (previouslyConnected, nowConnected) ->
                            if (previouslyConnected == true && !nowConnected) {
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

    suspend fun requestTryReconnect() {
        onWebsocketError.emit(Unit)
    }

    sealed interface WebsocketData<T> {
        class Subscribe<T> : WebsocketData<T>

        class Disconnect<T> : WebsocketData<T>

        class Message<T>(val message: T) : WebsocketData<T>
    }
}

