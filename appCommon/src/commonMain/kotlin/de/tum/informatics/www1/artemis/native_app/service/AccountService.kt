package de.tum.informatics.www1.artemis.native_app.service

import kotlinx.coroutines.flow.Flow

/**
 * Service that provides data about the users login status.
 */
interface AccountService {

    /**
     * The latest authentication data. Flow emits a new element whenever the login status of the user changes.
     */
    val authenticationData: Flow<AuthenticationData>

    /**
     * Can either be [LoggedIn] or [NotLoggedIn].
     */
    sealed class AuthenticationData {
        object NotLoggedIn : AuthenticationData()

        data class LoggedIn(val authToken: String) : AuthenticationData() {
            val asBearer = "Bearer $authToken"
        }
    }

    suspend fun login(username: String, password: String, rememberMe: Boolean): LoginResponse

    data class LoginResponse(val isSuccessful: Boolean)

    /**
     * Deletes the JWT.
     */
    suspend fun logout()
}