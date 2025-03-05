package de.tum.informatics.www1.artemis.native_app.feature.quiz.service.impl

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.Api
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.QuizSubmission
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Submission
import de.tum.informatics.www1.artemis.native_app.feature.quiz.service.QuizParticipationService
import io.ktor.client.call.body
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
                        *Api.Quiz.path,
                        "exercises",
                        exerciseId.toString(),
                        "submissions",
                        endPoint
                    )
                }

                contentType(ContentType.Application.Json)
                cookieAuth(authToken)
                setBody<Submission>(submission)
            }.body()
        }
    }
}