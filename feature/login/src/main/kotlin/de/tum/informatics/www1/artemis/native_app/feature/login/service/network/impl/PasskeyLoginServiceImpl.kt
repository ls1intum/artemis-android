package de.tum.informatics.www1.artemis.native_app.feature.login.service.network.impl

import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.ServerSelectedBasedServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.login.service.network.LoginService
import de.tum.informatics.www1.artemis.native_app.feature.login.service.network.PasskeyLoginService
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.appendPathSegments
import io.ktor.http.isSuccess
import io.ktor.http.setCookie

class PasskeyLoginServiceImpl(
    ktorProvider: KtorProvider,
    artemisContextProvider: ArtemisContextProvider,
) : ServerSelectedBasedServiceImpl(ktorProvider, artemisContextProvider), PasskeyLoginService {

    private var setCookie: String = ""

    override suspend fun getAuthenticationOptions(): NetworkResponse<String> {
        val artemisContext = artemisContext()

        return performNetworkCall {
            val response = ktorProvider.ktorClient.post(artemisContext.serverUrl) {
                url {
                    appendPathSegments("webauthn", "authenticate", "options")
                }
            }

            if (response.status.isSuccess()) {
                setCookie = response.setCookie().firstOrNull { it.name == "jwt" }?.value ?: ""
                response.bodyAsText()
            } else {
                throw Exception("Failed to get authentication options: ${response.status}")
            }
        }
    }

    override suspend fun loginWithPasskey(publicKeyCredentialJson: String): NetworkResponse<LoginService.LoginResponse> {
        val artemisContext = artemisContext()

        return performNetworkCall {
            val response = ktorProvider.ktorClient.post(artemisContext.serverUrl) {
                url {
                    appendPathSegments("login", "webauthn")
                }

                cookieAuth(setCookie)
                setBody(publicKeyCredentialJson)
            }

            val jwt = response.setCookie().firstOrNull { it.name == "jwt" }?.value

            if (response.status.isSuccess() && jwt != null) {
                LoginService.LoginResponse(jwt)
            } else throw RuntimeException("Login not successful: ${response.status}")
        }
    }
}