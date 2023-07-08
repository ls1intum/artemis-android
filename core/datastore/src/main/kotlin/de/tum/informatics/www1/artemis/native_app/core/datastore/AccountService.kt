package de.tum.informatics.www1.artemis.native_app.core.datastore

import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService.AuthenticationData.LoggedIn
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService.AuthenticationData.NotLoggedIn
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

        data class LoggedIn(val authToken: String, val username: String) : AuthenticationData()
    }

    /**
     * Stores the given access token permanently
     */
    suspend fun storeAccessToken(jwt: String, rememberMe: Boolean)

    data class LoginResponse(val isSuccessful: Boolean)

    /**
     * Deletes the JWT.
     */
    suspend fun logout()
}

val AccountService.authToken: Flow<String>
    get() = authenticationData.map {
        when (it) {
            is LoggedIn -> it.authToken
            NotLoggedIn -> ""
        }
    }.distinctUntilChanged()

val AccountService.AuthenticationData.isLoggedIn: Boolean
    get() = this is LoggedIn