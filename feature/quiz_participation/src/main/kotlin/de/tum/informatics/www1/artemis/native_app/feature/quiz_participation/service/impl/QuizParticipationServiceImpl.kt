package de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.service.impl

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.QuizSubmission
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Submission
import de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.service.QuizParticipationService
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class QuizParticipationServiceImpl(
    private val ktorProvider: KtorProvider
) : QuizParticipationService {

    override suspend fun submitForPractice(
        submission: QuizSubmission,
        exerciseId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Result> {
        return submitImpl(serverUrl, exerciseId, authToken, submission, "practice")
    }

    override suspend fun submitForLiveMode(
        submission: QuizSubmission,
        exerciseId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Submission> {
        return submitImpl(serverUrl, exerciseId, authToken, submission, "live")
    }

    private suspend inline fun <reified T> submitImpl(
        serverUrl: String,
        exerciseId: Long,
        authToken: String,
        submission: QuizSubmission,
        endPoint: String
    ): NetworkResponse<T> {
        return performNetworkCall {
            ktorProvider.ktorClient.post(serverUrl) {
                url {
                    appendPathSegments(
                        "api",
                        "exercises",
                        exerciseId.toString(),
                        "submissions",
                        endPoint
                    )
                }

                contentType(ContentType.Application.Json)
                bearerAuth(authToken)
                setBody<Submission>(submission)
            }.body()
        }
    }
}