package de.tum.informatics.www1.artemis.native_app.core.test

import de.tum.informatics.www1.artemis.native_app.core.websocket.WebsocketProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import org.hildan.krossbow.stomp.StompReceipt
import org.hildan.krossbow.stomp.headers.StompSendHeaders
import org.koin.dsl.module

val testWebsocketModule = module {
    single<WebsocketProvider> { TestWebsocketProvider() }
}

class TestWebsocketProvider : WebsocketProvider {

    override val connectionState: Flow<WebsocketProvider.WebsocketConnectionState> =
        flowOf(WebsocketProvider.WebsocketConnectionState.WithSession(true))
    override val isConnected: Flow<Boolean> = flowOf(true)

    override fun <T : Any> subscribe(
        channel: String,
        deserializer: DeserializationStrategy<T>
    ): Flow<WebsocketProvider.WebsocketData<T>> = flowOf(WebsocketProvider.WebsocketData.Subscribe())

    override fun <T : Any> subscribeMessage(
        channel: String,
        deserializer: DeserializationStrategy<T>
    ): Flow<T> = emptyFlow()

    override fun requestTryReconnect() = Unit

    override suspend fun <T> convertAndSend(
        headers: StompSendHeaders,
        body: T,
        serializer: SerializationStrategy<T>
    ): StompReceipt? = null
}
