package de.tum.informatics.www1.artemis.native_app.core.websocket.impl

import android.util.Log
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.JsonProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.DeserializationStrategy
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.config.HeartBeat
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import org.hildan.krossbow.stomp.conversions.kxserialization.*
import org.hildan.krossbow.stomp.conversions.kxserialization.json.withJsonConversions
import org.hildan.krossbow.stomp.headers.StompSubscribeHeaders
import org.hildan.krossbow.websocket.ktor.KtorWebSocketClient
import java.net.URLEncoder
import kotlin.time.Duration.Companion.minutes

internal class WebsocketProvider(
    ktorProvider: KtorProvider,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
    private val jsonProvider: JsonProvider
) {

    companion object {
        private const val TAG = "WebsocketProvider"
    }

    private val client =
        StompClient(KtorWebSocketClient()) {
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
            serverConfigurationService.host,
            accountService.authenticationData
        ) { a, b -> a to b }
            .transformLatest { (host, authenticationData) ->
                println("host: $host; authData=$authenticationData")
                emitAll(
                    channelFlow {
                        println("START WEBSOCKET")
                        val url =
                            "wss://$host/websocket/tracker/websocket" + when (authenticationData) {
                                is AccountService.AuthenticationData.LoggedIn -> {
                                    "?access_token=${authenticationData.authToken}"
                                }
                                AccountService.AuthenticationData.NotLoggedIn -> ""
                            }

                        val session =
                            client.connect(url)
                                .withJsonConversions(jsonProvider.networkJsonConfiguration)
                        send(session)

                        awaitClose {
                            println("STOP WEBSOCKET")

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
                    .retryWhen { _, attempt ->
                        delay(2.seconds * attempt.toInt())
                        true
                    }
                emitAll(flow)
            }
    }
}

