package de.tum.informatics.www1.artemis.native_app.feature.login.service.network

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.feature.login.service.LoginOptionsDto
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface LoginService {

    /**
     * Perform a login request to the server.
     */
    suspend fun loginWithCredentials(username: String, password: String, rememberMe: Boolean, serverUrl: String): NetworkResponse<LoginResponse>

    suspend fun loginSaml2(rememberMe: Boolean, serverUrl: String): NetworkResponse<HttpResponse>

    suspend fun fetchLoginOptions(usernameOrEmail: String, serverUrl: String): NetworkResponse<LoginOptionsDto>

    suspend fun exchangeCodeForJwtToken(code: String, codeVerifier: String, serverUrl: String): NetworkResponse<LoginResponse>
    @Serializable
    data class LoginResponse(
        @SerialName("id_token") val idToken: String
    )
}
