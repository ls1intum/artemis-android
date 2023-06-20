package de.tum.informatics.www1.artemis.native_app.core.data.service.impl

import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlin.time.Duration.Companion.seconds

internal class KtorProviderImpl(jsonProvider: JsonProvider) : KtorProvider {

    override val ktorClient = HttpClient {
        install(ContentNegotiation) {
            json(jsonProvider.applicationJsonConfiguration)
        }
        install(WebSockets)

        install(HttpTimeout) {
            requestTimeoutMillis = 15.seconds.inWholeMilliseconds
            connectTimeoutMillis = 10.seconds.inWholeMilliseconds
            socketTimeoutMillis = 10.seconds.inWholeMilliseconds
        }
    }
}