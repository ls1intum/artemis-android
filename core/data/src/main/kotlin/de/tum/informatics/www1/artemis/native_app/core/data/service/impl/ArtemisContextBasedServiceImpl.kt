package de.tum.informatics.www1.artemis.native_app.core.data.service.impl

import android.util.Log
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
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


const val TAG = "ArtemisContextBasedServiceImpl"

abstract class ArtemisContextBasedServiceImpl(
    val ktorProvider: KtorProvider,
    private val artemisContextProvider: ArtemisContextProvider,
) : ArtemisContextBasedService {

    override val onReloadRequired: Flow<Unit> = artemisContextProvider.stateFlow.map { Unit }

    val artemisContext: ArtemisContext
        get() = artemisContextProvider.stateFlow.value

    val serverUrl: String
        get() = artemisContext.serverUrl

    val authToken: String
        get() = artemisContext.authToken

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

    suspend inline fun <reified T: Any>deleteRequest(
        contentType: ContentType = ContentType.Application.Json,
        crossinline block: HttpRequestBuilder.() -> Unit
    ): NetworkResponse<T> {
        return request(contentType) {
            method = HttpMethod.Delete
            block()
        }
    }

    suspend inline fun <reified T: Any>request(
        contentType: ContentType = ContentType.Application.Json,
        crossinline block: HttpRequestBuilder.() -> Unit
    ): NetworkResponse<T> {
        return performNetworkCall {
            val response = ktorProvider.ktorClient.request(serverUrl) {
                block()
                contentType(contentType)
                cookieAuth(authToken)
            }

            val requestString = "${response.request.method} ${response.request.url}"
            if (!response.status.isSuccess()) {
                Log.e(TAG, "${response.status.value} for $requestString: ${response.bodyAsText()}")
            } else {
                Log.d(TAG, requestString)
            }

            response.body()
        }
    }



}