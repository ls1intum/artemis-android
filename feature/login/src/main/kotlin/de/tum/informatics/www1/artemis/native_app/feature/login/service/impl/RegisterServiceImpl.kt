package de.tum.informatics.www1.artemis.native_app.feature.login.service.impl

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.feature.login.service.RegisterService
import io.ktor.client.request.*
import io.ktor.http.*

internal class RegisterServiceImpl(
    private val ktorProvider: KtorProvider
) : RegisterService {

    override suspend fun register(account: User, serverUrl: String): NetworkResponse<HttpStatusCode> {
        return performNetworkCall {
            ktorProvider.ktorClient.post(serverUrl) {
                url {
                    appendPathSegments("api", "register")
                }

                setBody(account)
                contentType(ContentType.Application.Json)
            }.status
        }
    }
}