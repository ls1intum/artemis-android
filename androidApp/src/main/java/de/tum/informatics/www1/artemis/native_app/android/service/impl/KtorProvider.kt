package de.tum.informatics.www1.artemis.native_app.android.service.impl

import de.tum.informatics.www1.artemis.native_app.android.content.exercise.exerciseSerializerModule
import de.tum.informatics.www1.artemis.native_app.android.content.lecture.lectureSerializerModule
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

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
    }
}