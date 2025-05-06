package de.tum.informatics.www1.artemis.native_app.core.data.service.passkey

import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.ServerSelectedBasedServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.data.service.passkey.dto.PasskeyLoginResponseDTO
import de.tum.informatics.www1.artemis.native_app.core.data.service.passkey.dto.RegisterPasskeyDTO
import de.tum.informatics.www1.artemis.native_app.core.data.service.passkey.dto.RegisterPasskeyResponseDTO
import io.ktor.client.request.setBody
import io.ktor.http.appendPathSegments

// Inspired by the webapp implementation: https://github.com/ls1intum/Artemis/blob/develop/src/main/webapp/app/core/user/settings/passkey-settings/webauthn-api.service.ts
// Note: The requests do not use the Api.kt, because they do not include the /api prefix.
class WebauthnApiService(
    ktorProvider: KtorProvider,
    artemisContextProvider: ArtemisContextProvider,
) : ServerSelectedBasedServiceImpl(ktorProvider, artemisContextProvider){

    suspend fun getRegistrationOptions(): NetworkResponse<String> {
        return postRequest {
            url {
                appendPathSegments("webauthn", "register", "options")
            }
        }
    }

    suspend fun registerPasskey(registerPasskeyDto: RegisterPasskeyDTO): NetworkResponse<RegisterPasskeyResponseDTO> {
        return postRequest {
            url {
                appendPathSegments("webauthn", "register")
            }
            setBody(registerPasskeyDto)
        }
    }

    suspend fun getAuthenticationOptions(): NetworkResponse<String> {
        return postRequest {
            url {
                appendPathSegments("webauthn", "authenticate", "options")
            }
        }
    }

    suspend fun loginWithPasskey(publicKeyCredentialJson: String): NetworkResponse<PasskeyLoginResponseDTO> {
        return postRequest {
            url {
                appendPathSegments("login", "webauthn")
            }
            setBody(publicKeyCredentialJson)
        }
    }

}