package de.tum.informatics.www1.artemis.native_app.android.service.impl

import de.tum.informatics.www1.artemis.native_app.android.service.AccountService
import de.tum.informatics.www1.artemis.native_app.android.service.ServerCommunicationProvider
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
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


class WebsocketProvider(
    ktorProvider: KtorProvider,
    private val serverCommunicationProvider: ServerCommunicationProvider,
    private val accountService: AccountService,
    private val jsonProvider: JsonProvider
) {

    private val client =
        StompClient(webSocketClient = KtorWebSocketClient(ktorProvider.ktorClient)) {
            heartBeat = HeartBeat(10.seconds, 10.seconds)
            gracefulDisconnect = false
        }

    /**
     * Connects a stomp session only if it is actually needed.
     * After 10 seconds of not having any subscribers, the session will be closes
     */
    @OptIn(DelicateCoroutinesApi::class)
    private val session: Flow<StompSessionWithKxSerialization> =
        combine(
            serverCommunicationProvider.serverUrl,
            accountService.authenticationData
        ) { a, b -> a to b }
            .transformLatest { (serverUrl, authenticationData) ->
                emitAll(
                    channelFlow {
                        val url = "$serverUrl/websocket/tracker" + when (authenticationData) {
                            is AccountService.AuthenticationData.LoggedIn -> "?access_token=${authenticationData.authToken}"
                            AccountService.AuthenticationData.NotLoggedIn -> ""
                        }

                        val session = client.connect(url).withJsonConversions(jsonProvider.networkJsonConfiguration)
                        send(session)

                        awaitClose {
                            //Graceful disconnect is disabled, runBlocking should not block
                            runBlocking {
                                session.disconnect()
                            }
                        }
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
                val flow: Flow<T> = currentSession.subscribe(StompSubscribeHeaders(destination = channel), deserializer)
                emitAll(flow)
            }
    }
}

