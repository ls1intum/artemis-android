package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.service.impl

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Submission
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.TextSubmission
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.service.TextSubmissionService
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class TextSubmissionServiceImpl(
    private val ktorProvider: KtorProvider
) : TextSubmissionService {
    override suspend fun create(
        textSubmission: TextSubmission,
        exerciseId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<TextSubmission> {
        return performNetworkCall {
            ktorProvider.ktorClient.post(serverUrl) {
                url {
                    appendPathSegments("api", "exercises", exerciseId.toString(), "text-submissions")
                }
                contentType(ContentType.Application.Json)
                setBody(textSubmission)
                bearerAuth(authToken)
            }.body()
        }
    }

    override suspend fun update(
        textSubmission: TextSubmission,
        exerciseId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<TextSubmission> {
        return performNetworkCall {
            ktorProvider.ktorClient.put(serverUrl) {
                url {
                    appendPathSegments("api", "exercises", exerciseId.toString(), "text-submissions")
                }
                contentType(ContentType.Application.Json)
                setBody<Submission>(textSubmission)
                bearerAuth(authToken)
            }.body()
        }
    }
}