package de.tum.informatics.www1.artemis.native_app.feature.login.service.network.impl

import android.util.Log
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.Api
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.feature.login.service.network.LoginService
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.http.setCookie
import kotlinx.serialization.Serializable

internal class LoginServiceImpl(
    private val ktorProvider: KtorProvider
) : LoginService {

    companion object {
        private const val TAG = "LoginServiceImpl"
    }

    override suspend fun loginWithCredentials(
        username: String,
        password: String,
        rememberMe: Boolean,
        serverUrl: String
    ): NetworkResponse<LoginService.LoginResponse> {
        return performNetworkCall {
            Log.d(TAG, "Logging in with credentials to serverUrl=$serverUrl")

            val response = ktorProvider.ktorClient.post(serverUrl) {
                url {
                    appendPathSegments(*Api.Core.Public.path, "authenticate")
                }

                contentType(ContentType.Application.Json)
                setBody(LoginBody(username, password, rememberMe))
            }

            val jwt = response.setCookie().firstOrNull { it.name == "jwt" }?.value

            if (response.status.isSuccess() && jwt != null) {
                LoginService.LoginResponse(jwt)
            } else throw RuntimeException("Login not successful: ${response.status}")
        }
    }

    override suspend fun loginSaml2(
        rememberMe: Boolean,
        serverUrl: String
    ): NetworkResponse<HttpResponse> {
        return performNetworkCall {
            ktorProvider.ktorClient.post(serverUrl) {
                url {
                    appendPathSegments(*Api.Core.Public.path, "saml2")
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