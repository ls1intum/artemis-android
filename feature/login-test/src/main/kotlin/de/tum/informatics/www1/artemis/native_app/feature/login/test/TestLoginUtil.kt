package de.tum.informatics.www1.artemis.native_app.feature.login.test

import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.feature.login.service.network.LoginService
import kotlinx.coroutines.flow.first
import org.koin.test.KoinTest
import org.koin.test.get

val user1Username: String
    get() = System.getenv("USER_1_USERNAME") ?: "aa01aaa"

val user1Password: String
    get() = System.getenv("USER_1_PASSWORD") ?: "test_user_1_password"

val user2Username: String get() = System.getenv("USER_2_USERNAME") ?: "aa02aaa"
val user3Username: String get() = System.getenv("USER_3_USERNAME") ?: "aa03aaa"
val user2DisplayName: String get() = System.getenv("USER_2_DISPLAY_NAME") ?: "Test User2"

suspend fun KoinTest.performTestLogin(): String {
    val loginService: LoginService = get()
    val accountService: AccountService = get()
    val serverConfigurationService: ServerConfigurationService = get()

    val serverUrl = serverConfigurationService.serverUrl.first()

    println("Logging in with credentials: username=$user1Username; password=$user1Password; serverUrl=$serverUrl")
    val response = loginService.loginWithCredentials(
        username = user1Username,
        password = user1Password,
        rememberMe = true,
        serverUrl = serverUrl
    )
    val loginResponse = response.orThrow("Login not successful")
    accountService.storeAccessToken(loginResponse.idToken, true)

    return loginResponse.idToken
}

suspend fun KoinTest.getAdminAccessToken(): String {
    val adminLoginService: AdminLoginService = get()
    return adminLoginService.getAdminJwt()
}