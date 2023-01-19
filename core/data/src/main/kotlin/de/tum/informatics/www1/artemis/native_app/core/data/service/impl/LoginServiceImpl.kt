package de.tum.informatics.www1.artemis.native_app.core.data.service.impl

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.LoginService
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

internal class LoginServiceImpl(
    private val ktorProvider: KtorProvider
) : LoginService {

    override suspend fun loginWithCredentials(
        username: String,
        password: String,
        rememberMe: Boolean,
        serverUrl: String
    ): NetworkResponse<LoginService.LoginResponse> {
        return performNetworkCall {
            val body: LoginService.LoginResponse =
                ktorProvider.ktorClient.post(serverUrl) {
                    url {
                        appendPathSegments("api", "authenticate")
                    }

                    contentType(ContentType.Application.Json)
                    setBody(LoginBody(username, password, rememberMe))
                }.body()

            body
        }
    }

    override suspend fun loginSaml2(rememberMe: Boolean, serverUrl: String): NetworkResponse<HttpResponse> {
        return performNetworkCall {
            ktorProvider.ktorClient.post(serverUrl) {
                url {
                    appendPathSegments("api", "saml2")
                }
                setBody(rememberMe)
                contentType(ContentType.Application.Json)
            }
        }
    }

    @Serializable
    private data class LoginBody(
        val username: String,
        val password: String,
        val rememberMe: Boolean
    )
}