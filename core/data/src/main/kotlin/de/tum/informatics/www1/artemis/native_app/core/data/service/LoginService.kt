package de.tum.informatics.www1.artemis.native_app.core.data.service

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface LoginService {

    /**
     * Perform a login request to the server.
     */
    suspend fun login(username: String, password: String, rememberMe: Boolean, serverUrl: String): NetworkResponse<LoginResponse>

    @Serializable
    data class LoginResponse(
        @SerialName("id_token") val idToken: String
    )
}