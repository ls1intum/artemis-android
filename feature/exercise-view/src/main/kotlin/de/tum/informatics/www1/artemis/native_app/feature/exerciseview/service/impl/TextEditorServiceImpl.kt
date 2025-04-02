package de.tum.informatics.www1.artemis.native_app.feature.exerciseview.service.impl

import de.tum.informatics.www1.artemis.native_app.core.data.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.Api
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.LoggedInBasedServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.service.TextEditorService
import io.ktor.http.appendPathSegments

class TextEditorServiceImpl(
    ktorProvider: KtorProvider,
    artemisContextProvider: ArtemisContextProvider,
) : LoggedInBasedServiceImpl(ktorProvider, artemisContextProvider), TextEditorService {

    override suspend fun getParticipation(participationId: Long): NetworkResponse<Participation> {
        return getRequest {
            url {
                appendPathSegments(*Api.Text.path, "text-editor", participationId.toString())
            }
        }
    }
}