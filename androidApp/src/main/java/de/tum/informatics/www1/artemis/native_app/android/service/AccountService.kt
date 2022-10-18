package de.tum.informatics.www1.artemis.native_app.android.service

import de.tum.informatics.www1.artemis.native_app.android.content.account.Account
import de.tum.informatics.www1.artemis.native_app.android.util.DataState
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

        /**
         * @property account the account information about the user that is logged in.
         */
        data class LoggedIn(val authToken: String, val account: DataState<Account>) : AuthenticationData() {
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