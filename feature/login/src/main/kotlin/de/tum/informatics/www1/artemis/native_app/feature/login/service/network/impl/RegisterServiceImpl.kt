package de.tum.informatics.www1.artemis.native_app.feature.login.service.network.impl

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.Api
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.feature.login.service.network.RegisterService
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType

internal class RegisterServiceImpl(
    private val ktorProvider: KtorProvider
) : RegisterService {

    override suspend fun register(
        account: User,
        serverUrl: String
    ): NetworkResponse<HttpStatusCode> {
        return performNetworkCall {
            ktorProvider.ktorClient.post(serverUrl) {
                url {
                    appendPathSegments(*Api.Core.Public.path, "register")
                }

                setBody(account)
                contentType(ContentType.Application.Json)
            }.status
        }
    }
}