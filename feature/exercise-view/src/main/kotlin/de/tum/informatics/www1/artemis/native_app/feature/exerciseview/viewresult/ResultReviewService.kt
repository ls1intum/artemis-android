package de.tum.informatics.www1.artemis.native_app.feature.exerciseview.viewresult

import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.ModelingExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.TextExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.FileUploadSubmission
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.ModelingSubmission
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.ProgrammingSubmission
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.TextSubmission
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.appendPathSegments
import io.ktor.http.isSuccess

internal data class ReviewContent(val submission: ReviewSubmission?, val feedback: List<ReviewFeedback>)
internal class ReviewHttpException(val status: Int) : Exception("Result review HTTP $status")

/** Every read uses one immutable account/server snapshot, including lazy long output and commit files. */
internal class ResultReviewService(private val ktorProvider: KtorProvider) {
    private suspend inline fun <reified T> read(session: ArtemisContext.LoggedIn, vararg path: String, query: Map<String, String> = emptyMap()): T {
        val response = ktorProvider.ktorClient.get(session.serverUrl) {
            cookieAuth(session.authToken)
            url {
                appendPathSegments(*path)
                query.forEach { (key, value) -> parameters.append(key, value) }
            }
        }
        if (!response.status.isSuccess()) throw ReviewHttpException(response.status.value)
        return response.body()
    }

    suspend fun load(session: ArtemisContext.LoggedIn, exercise: Exercise, target: ReviewTarget): ReviewContent {
        // This authorized result read gates all protected review content. It never uses a latest-result endpoint.
        val feedback = read<List<ReviewFeedback>>(session, "api", "assessment", "participations", target.participationId.toString(), "results", target.result.id.toString(), "details")
        val source = when (exercise) {
            is TextExercise -> {
                val response = read<ReviewParticipation>(session, "api", "text", "participations", target.participationId.toString(), "text-editor", query = mapOf("resultId" to target.result.id.toString()))
                check(response.id == target.participationId)
                response.submissions.firstOrNull { submission ->
                    (target.submissionId == null || submission.id == target.submissionId) && submission.results.any { it.id == target.result.id }
                } ?: throw ReviewHttpException(404)
            }
            is ModelingExercise -> {
                val submissions = read<List<ReviewSubmission>>(session, "api", "modeling", "participations", target.participationId.toString(), "submissions-with-results")
                val submission = submissions.firstOrNull { s -> (target.submissionId == null || target.submissionId == s.id) && s.results.any { it.id == target.result.id } }
                    ?: throw ReviewHttpException(404)
                if (target.result.assessmentType == Exercise.AssessmentType.MANUAL || target.result.assessmentType == Exercise.AssessmentType.SEMI_AUTOMATIC) {
                    val result = read<ReviewResult>(session, "api", "modeling", "modeling-submissions", submission.id.toString(), "result", query = mapOf("resultId" to target.result.id.toString()))
                    if (result.id != target.result.id) throw ReviewHttpException(404)
                }
                submission
            }
            else -> target.submission?.let { submission ->
                ReviewSubmission(
                    id = submission.id?.toLong(), submissionDate = submission.submissionDate?.toString(),
                    text = (submission as? TextSubmission)?.text,
                    filePath = (submission as? FileUploadSubmission)?.filePath,
                    model = (submission as? ModelingSubmission)?.model,
                    explanationText = (submission as? ModelingSubmission)?.explanationText,
                    commitHash = (submission as? ProgrammingSubmission)?.commitHash,
                )
            }
        }
        return ReviewContent(source, feedback)
    }

    suspend fun publishedSolution(session: ArtemisContext.LoggedIn, exerciseId: Long): Exercise {
        val solution = read<Exercise>(session, "api", "exercise", "exercises", exerciseId.toString(), "example-solution")
        require(solution.id == exerciseId)
        return solution
    }

    suspend fun longFeedback(session: ArtemisContext.LoggedIn, feedbackId: Long): String =
        read(session, "api", "assessment", "feedbacks", feedbackId.toString(), "long-feedback")

    suspend fun buildLogs(session: ArtemisContext.LoggedIn, target: ReviewTarget): List<ReviewBuildLog> =
        read(session, "api", "programming", "participations", target.participationId.toString(), "buildlogs", query = mapOf("resultId" to target.result.id.toString()))

    suspend fun commitFiles(session: ArtemisContext.LoggedIn, target: ReviewTarget, commit: String): List<String> {
        require(commit.matches(Regex("[a-fA-F0-9]{40}")))
        val files = read<Map<String, String>>(session, "api", "programming", "participations", target.participationId.toString(), "repository", "commits", commit, "files")
        return files.filterValues { it == "FILE" }.keys.sorted()
    }

    suspend fun commitFile(session: ArtemisContext.LoggedIn, target: ReviewTarget, commit: String, filename: String): String {
        require(commit.matches(Regex("[a-fA-F0-9]{40}")))
        return read<ByteArray>(session, "api", "programming", "participations", target.participationId.toString(), "repository", "commits", commit, "file", query = mapOf("file" to filename)).decodeToString()
    }

    suspend fun file(session: ArtemisContext.LoggedIn, url: String): ByteArray {
        require(reviewFileUrl(session.serverUrl, url) == url)
        // A redirect must not forward this account's Cookie header to a different origin.
        val client = ktorProvider.ktorClient.config { followRedirects = false; expectSuccess = false }
        return try {
            val response = client.get(url) { cookieAuth(session.authToken) }
            if (!response.status.isSuccess()) throw ReviewHttpException(response.status.value)
            response.body()
        } finally { client.close() }
    }
}
