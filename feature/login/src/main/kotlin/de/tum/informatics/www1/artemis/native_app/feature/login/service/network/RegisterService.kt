package de.tum.informatics.www1.artemis.native_app.feature.login.service.network

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import io.ktor.http.HttpStatusCode

/**
 * Allows the user to register
 */
internal interface RegisterService {

    /**
     * Registers a new user. This is only possible if the password is long enough and there is no other user with the
     * same username or e-mail.
     *
     * @param account The data object holding the information about the new user
     */
    suspend fun register(account: User, serverUrl: String): NetworkResponse<HttpStatusCode>
}