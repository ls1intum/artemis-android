package de.tum.informatics.www1.artemis.native_app.feature.quiz.service.impl

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.Api
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.QuizSubmission
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.feature.quiz.service.QuizExerciseService
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

    override suspend fun findResultParticipation(
        exerciseId: Long,
        serverUrl: String,
        authToken: String,
        participationId: Long?,
        submissionId: Int?,
    ): NetworkResponse<Participation> = performNetworkCall {
        val selected = if (participationId != null && submissionId != null) participationId to submissionId else {
            val details = ktorProvider.ktorClient.get(serverUrl) {
                cookieAuth(authToken)
                url { appendPathSegments("api", "exercise", "exercises", exerciseId.toString(), "details") }
            }.body<ExerciseDetails>()
            require(details.exercise.id == exerciseId)
            details.exercise.studentParticipations.orEmpty().flatMap { participation ->
                participation.submissions.orEmpty()
                    .filterIsInstance<QuizSubmission>()
                    .flatMap { submission -> submission.results.orEmpty().map { Triple(participation, submission, it) } }
            }.filter { it.first.id != null && it.second.id != null && it.third.id != null }
                .maxWithOrNull(compareBy<Triple<Participation, QuizSubmission, Result>> { it.third.completionDate }.thenBy { it.third.id })
                ?.let { it.first.id!! to it.second.id!! } ?: throw IllegalStateException("No evaluated quiz submission")
        }
        val participation = ktorProvider.ktorClient.get(serverUrl) {
            cookieAuth(authToken)
            url {
                appendPathSegments(*Api.Quiz.QuizExercises.path, exerciseId.toString(), "participations", selected.first.toString(), "result")
                parameters.append("submissionId", selected.second.toString())
            }
        }.body<Participation>()
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
            ktorProvider.ktorClient.get(serverUrl) {
                url {
                    appendPathSegments(*Api.Quiz.QuizExercises.path, exerciseId.toString(), "for-student")
                }

                contentType(ContentType.Application.Json)
                cookieAuth(authToken)
            }.body()
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