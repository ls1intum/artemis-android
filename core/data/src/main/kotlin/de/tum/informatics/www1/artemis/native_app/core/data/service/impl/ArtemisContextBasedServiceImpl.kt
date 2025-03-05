package de.tum.informatics.www1.artemis.native_app.core.data.service.impl

import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.ArtemisContextBasedService
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

abstract class ArtemisContextBasedServiceImpl(
    val ktorProvider: KtorProvider,
    private val artemisContextProvider: ArtemisContextProvider,
) : ArtemisContextBasedService {

    override val onReloadRequired: Flow<Unit> = artemisContextProvider.flow.map { Unit }

    suspend fun artemisContext(): ArtemisContext = artemisContextProvider.flow.first()
    suspend fun serverUrl(): String = artemisContext().serverUrl
    suspend fun authToken(): String = artemisContext().authToken

    suspend inline fun <reified T: Any>getRequest(
        contentType: ContentType = ContentType.Application.Json,
        crossinline block: HttpRequestBuilder.() -> Unit
    ): NetworkResponse<T> {
        return request(contentType) {
            method = HttpMethod.Get
            block()
        }
    }

    suspend inline fun <reified T: Any>postRequest(
        contentType: ContentType = ContentType.Application.Json,
        crossinline block: HttpRequestBuilder.() -> Unit
    ): NetworkResponse<T> {
        return request(contentType) {
            method = HttpMethod.Post
            block()
        }
    }

    suspend inline fun <reified T: Any>putRequest(
        contentType: ContentType = ContentType.Application.Json,
        crossinline block: HttpRequestBuilder.() -> Unit
    ): NetworkResponse<T> {
        return request(contentType) {
            method = HttpMethod.Put
            block()
        }
    }

    suspend inline fun <reified T: Any>request(
        contentType: ContentType = ContentType.Application.Json,
        crossinline block: HttpRequestBuilder.() -> Unit
    ): NetworkResponse<T> {
        return performNetworkCall {
            ktorProvider.ktorClient.request(serverUrl()) {
                block()
                contentType(contentType)
                cookieAuth(authToken())
            }.body()
        }
    }



}