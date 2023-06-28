package de.tum.informatics.www1.artemis.native_app.feature.login.test

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.feature.login.service.LoginService
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

    val response = loginService.loginWithCredentials("", "", true, "")
    val loginResponse: NetworkResponse.Response<LoginService.LoginResponse> =
        assertIs(response, "Login not successful.")
    accountService.storeAccessToken(loginResponse.data.idToken, true)
}