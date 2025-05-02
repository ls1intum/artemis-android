package de.tum.informatics.www1.artemis.native_app.core.data.service.passkey

import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.Api
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.LoggedInBasedServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.data.service.passkey.dto.PasskeyDTO
import io.ktor.http.appendPathSegments

class PasskeySettingsService(
    ktorProvider: KtorProvider,
    artemisContextProvider: ArtemisContextProvider,
) : LoggedInBasedServiceImpl(ktorProvider,artemisContextProvider){

    suspend fun getPasskeys(): NetworkResponse<List<PasskeyDTO>> {
        return getRequest {
            url {
                appendPathSegments(*Api.Core.Passkey.path, "user")
            }
        }
    }
}