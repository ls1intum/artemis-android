package de.tum.informatics.www1.artemis.native_app.feature.login.service.network.impl

import android.util.Log
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.Api
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.feature.login.service.LoginOptionsDto
import de.tum.informatics.www1.artemis.native_app.feature.login.service.network.LoginService
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
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

    override suspend fun fetchLoginOptions(
        usernameOrEmail: String,
        serverUrl: String
    ): NetworkResponse<LoginOptionsDto> {
        return performNetworkCall {
            val response = ktorProvider.ktorClient.get(serverUrl) {
                url {
                    appendPathSegments(*Api.Core.Public.path, "login-options")
                    parameter("usernameOrEmail", usernameOrEmail)
                }
            }
            if (response.status.isSuccess()) {
                response.body<LoginOptionsDto>()
            } else {
                throw RuntimeException("Failed to fetch login options: ${response.status}")
            }
        }
    }

    @Serializable
    private data class ExchangeCodeBody(
        val code: String,
        val codeVerifier: String
    )

    override suspend fun exchangeCodeForJwtToken(
        code: String,
        codeVerifier: String,
        serverUrl: String
    ): NetworkResponse<LoginService.LoginResponse> {
        return performNetworkCall {
            val response = ktorProvider.ktorClient.post(serverUrl) {
                url {
                    appendPathSegments(*Api.Core.Public.path, "exchange-code")
                }
                contentType(ContentType.Application.Json)
                setBody(ExchangeCodeBody(code, codeVerifier))
            }
            if (response.status.isSuccess()) {
                val rawToken = response.bodyAsText().trim()
                if (rawToken.isNotBlank()) {
                    LoginService.LoginResponse(rawToken)
                } else {
                    throw RuntimeException("Received empty token from server")
                }
            } else {
                throw RuntimeException("Failed to exchange code: ${response.status}")
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
