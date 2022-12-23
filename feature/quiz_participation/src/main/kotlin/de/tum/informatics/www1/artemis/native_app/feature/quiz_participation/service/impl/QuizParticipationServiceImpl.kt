package de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.service.impl

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.QuizSubmission
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Submission
import de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.service.QuizParticipationService
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType

class QuizParticipationServiceImpl(
    private val ktorProvider: KtorProvider
) : QuizParticipationService {

    override suspend fun submitForPractice(
        submission: QuizSubmission,
        exerciseId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Result> {
        return performNetworkCall {
            ktorProvider.ktorClient.post(serverUrl) {
                url {
                    appendPathSegments(
                        "api",
                        "exercises",
                        exerciseId.toString(),
                        "submissions",
                        "practice"
                    )
                }

                contentType(ContentType.Application.Json)
                bearerAuth(authToken)
                setBody<Submission>(submission)
            }.body()
        }
    }

    override suspend fun submitForLiveMode(
        submission: QuizSubmission,
        exerciseId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Submission> {
        return performNetworkCall {
            ktorProvider.ktorClient.post(serverUrl) {
                url {
                    appendPathSegments(
                        "api",
                        "exercises",
                        exerciseId.toString(),
                        "submissions",
                        "live"
                    )
                }

                contentType(ContentType.Application.Json)
                bearerAuth(authToken)
                setBody<Submission>(submission)
            }.body()
        }
    }
}