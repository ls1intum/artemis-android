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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.reflect.KClass


const val TAG = "ArtemisContextBasedServiceImpl"

abstract class ArtemisContextBasedServiceImpl(
    val ktorProvider: KtorProvider,
    artemisContextProvider: ArtemisContextProvider,
    contextClass: KClass<out ArtemisContext> = ArtemisContext.LoggedIn::class,
) : ArtemisContextBasedService {

    private val filteredArtemisContextFlow: Flow<ArtemisContext> = artemisContextProvider.stateFlow
        .filterIsInstance(contextClass)

    override val onReloadRequired: Flow<Unit> = filteredArtemisContextFlow
        .distinctUntilChanged()
        .map { Unit }

    suspend fun artemisContext(): ArtemisContext = filteredArtemisContextFlow.first()

    suspend fun serverUrl(): String = artemisContext().serverUrl

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
        val serverUrl = serverUrl()
        val authToken = when (val context = artemisContext()) {
            is ArtemisContext.LoggedIn -> context.authToken
            else -> ""
        }

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