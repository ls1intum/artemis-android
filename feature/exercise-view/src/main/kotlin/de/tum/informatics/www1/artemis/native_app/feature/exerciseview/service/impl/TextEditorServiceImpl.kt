package de.tum.informatics.www1.artemis.native_app.feature.exerciseview.service.impl

import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.Api
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.LoggedInBasedServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.service.dto.TextEditorParticipationDto
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.service.TextEditorService
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.appendPathSegments

class TextEditorServiceImpl(
    ktorProvider: KtorProvider,
    artemisContextProvider: ArtemisContextProvider,
) : LoggedInBasedServiceImpl(ktorProvider, artemisContextProvider), TextEditorService {

    override suspend fun getParticipation(participationId: Long, session: ArtemisContext.LoggedIn?): NetworkResponse<Participation> {
        val pinned = session ?: artemisContext()
        return performNetworkCall {
            ktorProvider.ktorClient.get(pinned.serverUrl) {
                url { appendPathSegments(*Api.Text.Participations.path, participationId.toString(), "text-editor") }
                cookieAuth(pinned.authToken)
            }.body<TextEditorParticipationDto>().toParticipation()
        }
    }
}
