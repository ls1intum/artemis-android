package de.tum.informatics.www1.artemis.native_app.core.websocket

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import org.hildan.krossbow.stomp.StompReceipt
import org.hildan.krossbow.stomp.headers.StompSendHeaders

class WebsocketProviderStub : WebsocketProvider {

    override val connectionState: Flow<WebsocketProvider.WebsocketConnectionState> =
        flowOf(WebsocketProvider.WebsocketConnectionState.WithSession(true))
    override val isConnected: Flow<Boolean> = flowOf(true)

    override fun <T : Any> subscribe(
        topic: String,
        deserializer: DeserializationStrategy<T>
    ): Flow<WebsocketProvider.WebsocketData<T>> = emptyFlow()

    override fun <T : Any> subscribeMessage(
        topic: String,
        deserializer: DeserializationStrategy<T>
    ): Flow<T> = emptyFlow()

    override suspend fun <T> convertAndSend(
        headers: StompSendHeaders,
        body: T,
        serializer: SerializationStrategy<T>
    ): StompReceipt? = null

    override fun requestTryReconnect() = Unit
}
