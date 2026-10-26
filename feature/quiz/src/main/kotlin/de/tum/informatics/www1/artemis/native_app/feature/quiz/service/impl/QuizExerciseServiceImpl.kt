package de.tum.informatics.www1.artemis.native_app.feature.quiz.service.impl

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.Api
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.studentReviewGet
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.QuizSubmission
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.feature.quiz.service.QuizExerciseService
import de.tum.informatics.www1.artemis.native_app.feature.quiz.view_result.QuizReviewAttempt
import de.tum.informatics.www1.artemis.native_app.feature.quiz.view_result.quizReviewAttempts
import de.tum.informatics.www1.artemis.native_app.feature.quiz.view_result.initialQuizReviewAttempt
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

internal class QuizExerciseServiceImpl(
    private val ktorProvider: KtorProvider
) : QuizExerciseService {

    @Serializable
    private data class ExerciseDetails(val exercise: QuizExercise)

    override suspend fun findReviewAttempts(exerciseId: Long, serverUrl: String, authToken: String): NetworkResponse<List<QuizReviewAttempt>> = performNetworkCall {
        val details = ktorProvider.studentReviewGet<ExerciseDetails>(serverUrl, authToken, "api", "exercise", "exercises", exerciseId.toString(), "details")
        require(details.exercise.id == exerciseId)
        quizReviewAttempts(details.exercise)
    }

    override suspend fun findResultParticipation(
        exerciseId: Long,
        serverUrl: String,
        authToken: String,
        participationId: Long?,
        submissionId: Int?,
    ): NetworkResponse<Participation> = performNetworkCall {
        val selected = if (participationId != null && submissionId != null) participationId to submissionId else {
            val details = ktorProvider.studentReviewGet<ExerciseDetails>(serverUrl, authToken, "api", "exercise", "exercises", exerciseId.toString(), "details")
            require(details.exercise.id == exerciseId)
            initialQuizReviewAttempt(quizReviewAttempts(details.exercise))?.let { it.participationId to it.submissionId }
                ?: throw IllegalStateException("No evaluated quiz submission")
        }
        val participation = ktorProvider.studentReviewGet<Participation>(serverUrl, authToken,
            *Api.Quiz.QuizExercises.path, exerciseId.toString(), "participations", selected.first.toString(), "result",
            query = mapOf("submissionId" to selected.second.toString()))
        require(participation.id == selected.first && participation.exercise?.id == exerciseId)
        require(participation.submissions?.singleOrNull()?.id == selected.second)
        participation
    }

    override suspend fun findForStudent(
        exerciseId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<QuizExercise> {
        return performNetworkCall {
            ktorProvider.studentReviewGet<QuizExercise>(serverUrl, authToken, *Api.Quiz.QuizExercises.path, exerciseId.toString(), "for-student")
        }
    }

    override suspend fun join(
        exerciseId: Long,
        password: String,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<QuizExercise.QuizBatch> {
        return performNetworkCall {
            ktorProvider.ktorClient.post(serverUrl) {
                url {
                    appendPathSegments(*Api.Quiz.QuizExercises.path, exerciseId.toString(), "join")
                }

                setBody(Password(password))
                contentType(ContentType.Application.Json)
                cookieAuth(authToken)
            }.body()
        }
    }

    @Serializable
    private data class Password(val password: String)
}