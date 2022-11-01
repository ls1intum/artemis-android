package de.tum.informatics.www1.artemis.native_app.android.service.impl

import android.util.Log
import de.tum.informatics.www1.artemis.native_app.android.service.AccountService
import de.tum.informatics.www1.artemis.native_app.android.service.ServerCommunicationProvider
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.config.HeartBeat
import org.hildan.krossbow.websocket.ktor.KtorWebSocketClient
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import org.hildan.krossbow.stomp.conversions.kxserialization.*
import org.hildan.krossbow.stomp.conversions.kxserialization.json.withJsonConversions
import org.hildan.krossbow.stomp.headers.StompSubscribeHeaders
import org.hildan.krossbow.stomp.use
import org.hildan.krossbow.websocket.WebSocketClient
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import java.net.URLEncoder
import java.nio.charset.Charset
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.minutes


class WebsocketProvider(
    ktorProvider: KtorProvider,
    private val serverCommunicationProvider: ServerCommunicationProvider,
    private val accountService: AccountService,
    private val jsonProvider: JsonProvider
) {

    companion object {
        private const val TAG = "WebsocketProvider"
    }

    private val client =
        StompClient(OkHttpWebSocketClient()) {
            heartBeat = HeartBeat(10.seconds, 10.seconds)
            gracefulDisconnect = false
            connectionTimeout = 20.minutes
        }

    /**
     * Connects a stomp session only if it is actually needed.
     * After 10 seconds of not having any subscribers, the session will be closes
     */
    @OptIn(DelicateCoroutinesApi::class)
    private val session: Flow<StompSessionWithKxSerialization> =
        combine(
            serverCommunicationProvider.host,
            accountService.authenticationData
        ) { a, b -> a to b }
            .transformLatest { (host, authenticationData) ->
                emitAll(
                    channelFlow {
                        val url =
                            "wss://$host/websocket/tracker/websocket" + when (authenticationData) {
                                is AccountService.AuthenticationData.LoggedIn -> {
                                    val encodedAuthToken = URLEncoder.encode(
                                        authenticationData.authToken,
                                        Charsets.UTF_8.name()
                                    )
                                    "?access_token=$encodedAuthToken"
                                }
                                AccountService.AuthenticationData.NotLoggedIn -> ""
                            }

                        val session =
                            client.connect(url)
                                .withJsonConversions(jsonProvider.networkJsonConfiguration)
                        send(session)

                        awaitClose {
                            //Graceful disconnect is disabled, runBlocking should not block
                            runBlocking {
                                session.disconnect()
                            }
                        }
                    }
                        .retryWhen { e, attempt ->
                            Log.d(TAG, "Websocket connection failure (attempt $attempt): $e.")
                            val delayTime = when {
                                attempt > 20 -> 600
                                attempt > 16 -> 300
                                attempt > 12 -> 120
                                attempt > 8 -> 60
                                attempt > 4 -> 20
                                attempt > 2 -> 10
                                else -> 5
                            }.seconds

                            delay(delayTime)
                            true
                        }
                )
            }
            .shareIn(
                scope = GlobalScope, //We share this is the app container
                started = SharingStarted.WhileSubscribed(
                    stopTimeout = 10.seconds, replayExpiration = Duration.ZERO
                ),
                replay = 1
            )

    /**
     * Returns a flow that automatically unsubscribes once the collector is inactive.
     * The given flow can only be subscribed to once.
     */
    fun <T : Any> subscribe(channel: String, deserializer: DeserializationStrategy<T>): Flow<T> {
        return session
            .transformLatest { currentSession ->
                val flow: Flow<T> = currentSession.subscribe(
                    StompSubscribeHeaders(destination = channel),
                    deserializer
                )
                emitAll(flow)
            }
    }
}

