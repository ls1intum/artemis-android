package de.tum.informatics.www1.artemis.native_app.feature.login.test

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.feature.login.service.network.LoginService
import io.ktor.client.statement.HttpResponse

class DefaultLoginService() : LoginService {

    override suspend fun loginWithCredentials(
        username: String,
        password: String,
        rememberMe: Boolean,
        serverUrl: String
    ): NetworkResponse<LoginService.LoginResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun loginSaml2(
        rememberMe: Boolean,
        serverUrl: String
    ): NetworkResponse<HttpResponse> = throw NotImplementedError()
}