package de.tum.informatics.www1.artemis.native_app.core.websocket

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import org.hildan.krossbow.stomp.StompReceipt
import org.hildan.krossbow.stomp.headers.StompSendHeaders

interface WebsocketProvider {

    val connectionState: Flow<WebsocketConnectionState>

    val isConnected: Flow<Boolean>

    /**
     * Returns a flow that automatically unsubscribes once the collector is inactive.
     * The given flow can only be subscribed to once.
     *
     * Performs automatic reconnects.
     */
    fun <T : Any> subscribe(
        topic: String,
        deserializer: DeserializationStrategy<T>
    ): Flow<WebsocketData<T>>

    fun <T : Any> subscribeMessage(
        topic: String,
        deserializer: DeserializationStrategy<T>
    ): Flow<T>

    suspend fun <T> convertAndSend(headers: StompSendHeaders, body: T, serializer: SerializationStrategy<T>): StompReceipt?

    fun requestTryReconnect()

    sealed interface WebsocketData<T> {
        class Subscribe<T> : WebsocketData<T>

        class Disconnect<T> : WebsocketData<T>

        class Message<T>(val message: T) : WebsocketData<T>
    }

    sealed interface WebsocketConnectionState {
        val isConnected: Boolean

        /**
         * This is the start, no connection has been established yet
         */
        object Empty : WebsocketConnectionState {
            override val isConnected: Boolean = false
        }

        data class WithSession(override val isConnected: Boolean) : WebsocketConnectionState
    }
}

