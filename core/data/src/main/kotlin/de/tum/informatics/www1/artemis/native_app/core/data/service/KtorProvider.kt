package de.tum.informatics.www1.artemis.native_app.core.data.service

import io.ktor.client.HttpClient

/**
 * Provides instances for ktor.
 */
interface KtorProvider {

    /**
     * Http Client that should be used whenever making http requests to Artemis.
     * Can automatically decode JSON using kotlinx.serialization.
     */
    val ktorClient: HttpClient
}