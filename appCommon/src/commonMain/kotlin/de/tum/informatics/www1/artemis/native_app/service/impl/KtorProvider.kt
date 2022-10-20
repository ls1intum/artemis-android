package de.tum.informatics.www1.artemis.native_app.service.impl

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Provides instances for ktor.
 */
class KtorProvider {

    /**
     * The response json configuration.
     */
    private val json = Json {
        //Ignore unknown keys. If set to false, parsing JSON will throw an error when a new key is introduced.
        ignoreUnknownKeys = true
    }

    /**
     * Http Client that should be used whenever making http requests to Artemis.
     * Can automatically decode JSON using kotlinx.serialization.
     */
    val ktorClient = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
    }
}