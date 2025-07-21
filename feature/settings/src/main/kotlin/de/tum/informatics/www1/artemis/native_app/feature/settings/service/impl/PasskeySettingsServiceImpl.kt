package de.tum.informatics.www1.artemis.native_app.feature.settings.service.impl

import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.Api
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.LoggedInBasedServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.settings.service.PasskeySettingsService
import de.tum.informatics.www1.artemis.native_app.feature.settings.service.dto.PasskeyDTO
import de.tum.informatics.www1.artemis.native_app.feature.settings.service.dto.RegisterPasskeyResponseDTO
import io.ktor.client.request.setBody
import io.ktor.http.appendPathSegments
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

class PasskeySettingsServiceImpl(
    ktorProvider: KtorProvider,
    artemisContextProvider: ArtemisContextProvider,
) : LoggedInBasedServiceImpl(ktorProvider, artemisContextProvider), PasskeySettingsService {

    override suspend fun getPasskeys(): NetworkResponse<List<PasskeyDTO>> {
        return getRequest {
            url {
                appendPathSegments(*Api.Core.Passkey.path, "user")
            }
        }
    }

    override suspend fun getRegistrationOptions(): NetworkResponse<String> {
        return postRequest {
            url {
                appendPathSegments("webauthn", "register", "options")
            }
        }
    }

    @Serializable
    private data class RegisterRequest(val publicKey: PublicKey)

    @Serializable
    private data class PublicKey(val credential: JsonObject, val label: String)

    override suspend fun registerPasskey(registerPasskeyJson: String): NetworkResponse<RegisterPasskeyResponseDTO> {
        val credential = Json.parseToJsonElement(registerPasskeyJson).jsonObject
        val publicKey = PublicKey(credential = credential, label = "Passkey Android")
        return postRequest {
            url {
                appendPathSegments("webauthn", "register")
            }
            setBody(RegisterRequest(publicKey))
        }
    }

    override suspend fun deletePasskey(credentialId: String): NetworkResponse<Unit> {
        return deleteRequest {
            url {
                appendPathSegments("webauthn", "register", credentialId)
            }
        }
    }
}