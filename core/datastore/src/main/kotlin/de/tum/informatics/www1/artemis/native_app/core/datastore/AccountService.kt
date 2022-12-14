package de.tum.informatics.www1.artemis.native_app.core.datastore

import com.auth0.jwt.JWT
import de.tum.informatics.www1.artemis.native_app.core.model.account.Account
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

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

        data class LoggedIn(val authToken: String, val username: String) :
            AuthenticationData() {
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

val AccountService.authToken: Flow<String>
    get() = authenticationData.map {
        when (it) {
            is AccountService.AuthenticationData.LoggedIn -> it.authToken
            AccountService.AuthenticationData.NotLoggedIn -> ""
        }
    }.distinctUntilChanged()

val AccountService.AuthenticationData.isLoggedIn: Boolean
    get() = this is AccountService.AuthenticationData.LoggedIn