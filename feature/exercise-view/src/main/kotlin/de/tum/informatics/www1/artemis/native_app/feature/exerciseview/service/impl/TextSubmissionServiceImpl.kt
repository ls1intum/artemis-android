package de.tum.informatics.www1.artemis.native_app.feature.exerciseview.service.impl

import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.Api
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.LoggedInBasedServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Submission
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.TextSubmission
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.service.TextSubmissionService
import io.ktor.client.request.setBody
import io.ktor.http.appendPathSegments

class TextSubmissionServiceImpl(
    ktorProvider: KtorProvider,
    artemisContextProvider: ArtemisContextProvider,
) : LoggedInBasedServiceImpl(ktorProvider, artemisContextProvider), TextSubmissionService {

    override suspend fun update(
        textSubmission: TextSubmission,
        exerciseId: Long
    ): NetworkResponse<TextSubmission> {
        return putRequest {
            url {
                appendPathSegments(
                    *Api.Text.path,
                    "exercises",
                    exerciseId.toString(),
                    "text-submissions"
                )
            }
            setBody<Submission>(textSubmission)
        }
    }
}