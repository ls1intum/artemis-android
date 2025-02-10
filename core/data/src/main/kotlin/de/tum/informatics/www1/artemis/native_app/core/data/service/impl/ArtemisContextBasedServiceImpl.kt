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
import io.ktor.client.request.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

abstract class ArtemisContextBasedServiceImpl(
    val ktorProvider: KtorProvider,
    private val artemisContextProvider: ArtemisContextProvider,
) : ArtemisContextBasedService {

    override val onReloadRequired: Flow<Unit> = artemisContextProvider.current.map { Unit }

    suspend fun artemisContext(): ArtemisContext = artemisContextProvider.current.first()
    suspend fun serverUrl(): String = artemisContext().serverUrl
    suspend fun authToken(): String = artemisContext().authToken

    suspend inline fun <reified T: Any>get(
        crossinline block: HttpRequestBuilder.() -> Unit
    ): NetworkResponse<T> {
        return performNetworkCall {
            ktorProvider.ktorClient.get(serverUrl()) {
                block()

                cookieAuth(authToken())
            }.body()
        }
    }
}