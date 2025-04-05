package de.tum.informatics.www1.artemis.native_app.feature.settings.service

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.LoggedInBasedService
import de.tum.informatics.www1.artemis.native_app.core.model.account.Account
import io.ktor.http.ContentType

interface ChangeProfilePictureService: LoggedInBasedService {

    suspend fun upload(
        imageContentType: ContentType,
        fileBytes: ByteArray,
    ): NetworkResponse<Account>

    suspend fun delete(): NetworkResponse<Unit>
}