package de.tum.informatics.www1.artemis.native_app.core.data.service.impl

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.json.*
import kotlin.time.Duration.Companion.seconds

/**
 * Provides instances for ktor.
 */
class KtorProvider(jsonProvider: JsonProvider) {

    /**
     * Http Client that should be used whenever making http requests to Artemis.
     * Can automatically decode JSON using kotlinx.serialization.
     */
    val ktorClient = HttpClient {
        install(ContentNegotiation) {
            json(jsonProvider.networkJsonConfiguration)
        }
        install(WebSockets)

        install(HttpTimeout) {
            requestTimeoutMillis = 15.seconds.inWholeMilliseconds
            connectTimeoutMillis = 10.seconds.inWholeMilliseconds
            socketTimeoutMillis = 10.seconds.inWholeMilliseconds
        }
    }
}