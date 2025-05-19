package de.tum.informatics.www1.artemis.native_app.feature.settings.service

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.LoggedInBasedService
import de.tum.informatics.www1.artemis.native_app.feature.settings.service.dto.PasskeyDTO
import de.tum.informatics.www1.artemis.native_app.feature.settings.service.dto.RegisterPasskeyResponseDTO

interface PasskeySettingsService: LoggedInBasedService {

    suspend fun getPasskeys(): NetworkResponse<List<PasskeyDTO>>

    /**
     * Get the registration options for a passkey. This needs to be called to create a new passkey.
     */
    suspend fun getRegistrationOptions(): NetworkResponse<String>

    suspend fun registerPasskey(registerPasskeyJson: String): NetworkResponse<RegisterPasskeyResponseDTO>

}