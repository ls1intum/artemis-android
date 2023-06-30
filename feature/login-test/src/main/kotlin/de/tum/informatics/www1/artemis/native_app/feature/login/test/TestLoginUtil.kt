package de.tum.informatics.www1.artemis.native_app.feature.login.test

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.feature.login.service.LoginService
import kotlinx.coroutines.flow.first
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.test.assertIs

val testUsername: String
    get() = System.getenv("username") ?: "test_user"

val testPassword: String
    get() = System.getenv("password") ?: "test_user_password"

suspend fun KoinTest.performTestLogin() {
    val loginService: LoginService = get()
    val accountService: AccountService = get()
    val serverConfigurationService: ServerConfigurationService = get()

    val response = loginService.loginWithCredentials(
        testUsername,
        testPassword,
        true,
        serverConfigurationService.serverUrl.first()
    )
    val loginResponse: NetworkResponse.Response<LoginService.LoginResponse> =
        assertIs(response, "Login not successful.")
    accountService.storeAccessToken(loginResponse.data.idToken, true)
}

suspend fun KoinTest.getAdminAccessToken(): String {
    val adminLoginService: AdminLoginService = get()
    return adminLoginService.getAdminJwt()
}