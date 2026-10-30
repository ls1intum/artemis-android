package de.tum.informatics.www1.artemis.native_app.feature.exerciseview.service.impl

import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.Api
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.LoggedInBasedServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Submission
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.TextSubmission
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.service.TextSubmissionService
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.client.request.setBody
import io.ktor.http.appendPathSegments

class TextSubmissionServiceImpl(
    ktorProvider: KtorProvider,
    artemisContextProvider: ArtemisContextProvider,
) : LoggedInBasedServiceImpl(ktorProvider, artemisContextProvider), TextSubmissionService {

    override suspend fun update(
        textSubmission: TextSubmission,
        exerciseId: Long,
        session: ArtemisContext.LoggedIn?
    ): NetworkResponse<TextSubmission> {
        val pinned = session ?: artemisContext()
        return performNetworkCall {
            ktorProvider.ktorClient.request(pinned.serverUrl) {
                method = if (textSubmission.id == null) HttpMethod.Post else HttpMethod.Put
                url { appendPathSegments(*Api.Text.path, "exercises", exerciseId.toString(), "text-submissions") }
                contentType(ContentType.Application.Json)
                cookieAuth(pinned.authToken)
                setBody<Submission>(textSubmission)
            }.body<TextSubmission>()
        }
    }
}
