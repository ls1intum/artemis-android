package de.tum.informatics.www1.artemis.native_app.feature.login.test

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.feature.login.service.LoginService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.test.assertIs

class AdminLoginService(
    private val loginService: LoginService,
    private val serverConfigurationService: ServerConfigurationService
) {
    private val adminUsername: String get() = System.getenv("adminUsername") ?: "artemis_admin"
    private val adminPassword: String get() = System.getenv("adminPassword") ?: "artemis_admin"

    @Volatile
    private var cachedJwt: String? = null

    private val mutex = Mutex()

    suspend fun getAdminJwt(): String = mutex.withLock {
        val cachedJwt = cachedJwt
        if (cachedJwt != null) return cachedJwt

        val loginResponse: NetworkResponse.Response<LoginService.LoginResponse> = assertIs(
            loginService.loginWithCredentials(
                adminUsername,
                adminPassword,
                false,
                serverConfigurationService.serverUrl.first()
            )
        )

        this.cachedJwt = loginResponse.data.idToken

        loginResponse.data.idToken
    }
}