package de.tum.informatics.www1.artemis.native_app.feature.settings.service

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.ArtemisContextBasedService
import de.tum.informatics.www1.artemis.native_app.core.model.account.Account

interface ChangeProfilePictureService: ArtemisContextBasedService {

    suspend fun upload(
        fileBytes: ByteArray,
    ): NetworkResponse<Account>

    suspend fun delete(): NetworkResponse<Unit>
}