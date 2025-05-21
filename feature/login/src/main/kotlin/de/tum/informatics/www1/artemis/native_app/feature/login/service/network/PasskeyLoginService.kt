package de.tum.informatics.www1.artemis.native_app.feature.login.service.network

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse

interface PasskeyLoginService {

    /**
     * Gets the authentication options for the passkey login. This includes the login challenge.
     */
    suspend fun getAuthenticationOptions(): NetworkResponse<String>

    suspend fun loginWithPasskey(publicKeyCredentialJson: String): NetworkResponse<LoginService.LoginResponse>

}