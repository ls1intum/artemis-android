package de.tum.informatics.www1.artemis.native_app.feature.quiz.view_result

import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.StudentParticipation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.QuizSubmission
import kotlin.time.Instant

internal data class QuizReviewAttempt(
    val participationId: Long,
    val submissionId: Int,
    val resultId: Long,
    val rated: Boolean?,
    val practice: Boolean,
    val submissionDate: Instant?,
    val completionDate: Instant?,
)

internal fun quizReviewAttempts(exercise: QuizExercise): List<QuizReviewAttempt> = exercise.studentParticipations.orEmpty().flatMap { participation ->
    val pid = participation.id ?: return@flatMap emptyList()
    val nested = participation.submissions.orEmpty().filterIsInstance<QuizSubmission>().flatMap { submission -> submission.results.orEmpty().map { submission to it } }
    val direct = participation.results.orEmpty().mapNotNull { result -> (result.submission as? QuizSubmission)?.let { it to result } }
    (nested + direct).mapNotNull { (submission, result) ->
        val sid = submission.id ?: return@mapNotNull null
        val rid = result.id ?: return@mapNotNull null
        QuizReviewAttempt(pid, sid, rid, result.rated, (participation as? StudentParticipation)?.testRun == true, submission.submissionDate, result.completionDate)
    }
}.distinctBy { it.participationId to it.submissionId }
    .sortedWith(compareByDescending<QuizReviewAttempt> { it.completionDate }.thenByDescending { it.resultId })

/** The ordinary exercise result entry invokes the graded context; explicit/practice targets remain exact. */
internal fun initialQuizReviewAttempt(attempts: List<QuizReviewAttempt>): QuizReviewAttempt? =
    attempts.firstOrNull { it.rated == true && !it.practice } ?: attempts.firstOrNull { !it.practice } ?: attempts.firstOrNull()
