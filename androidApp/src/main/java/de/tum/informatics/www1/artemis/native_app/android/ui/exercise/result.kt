package de.tum.informatics.www1.artemis.native_app.android.ui.exercise

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import de.tum.informatics.www1.artemis.native_app.android.content.exercise.*
import de.tum.informatics.www1.artemis.native_app.android.content.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.android.content.exercise.submission.Result
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Enumeration object representing the possible options that
 * the status of the result's template can be in.
 */
private enum class ResultTemplateStatus {
    /**
     * An automatic result is currently being generated and should be available soon.
     * This is currently only relevant for programming exercises.
     */
    IS_BUILDING,

    /**
     * A regular, finished result is available.
     * Can be rated (counts toward the score) or not rated (after the deadline for practice).
     */
    HAS_RESULT,

    /**
     * There is no result or submission status that could be shown, e.g. because the student just started with the exercise.
     */
    NO_RESULT,

    /**
     * Submitted and the student can still continue to submit.
     */
    SUBMITTED,

    /**
     * Submitted and the student can no longer submit, but a result is not yet available.
     */
    SUBMITTED_WAITING_FOR_GRADING,

    /**
     * The student started the exercise but submitted too late.
     * Feedback is not yet available, and a future result will not count toward the score.
     */
    LATE_NO_FEEDBACK,

    /**
     * The student started the exercise and submitted too late, but feedback is available.
     */
    LATE,

    /**
     * No latest result available, e.g. because building took too long and the webapp did not receive it in time.
     * This is a distinct state because we want the student to know about this problematic state
     * and not confuse them by showing a previous result that does not match the latest submission.
     */
    MISSING
}

@Composable
fun ExerciseResult(
    modifier: Modifier,
    exercise: Exercise,
    participation: Participation,
    result: Result?,
    isBuilding: Boolean
) {
    val chosenResult: Result? = remember(participation, result) {
        result
            ?: participation.results.orEmpty()
                .maxByOrNull { it.completionDate ?: Instant.fromEpochSeconds(0L) }
    }

    val submission = result?.submission

    val templateStatus =
        remember(participation, exercise, chosenResult, isBuilding) {
            evaluateTemplateStatus(participation, exercise, chosenResult, isBuilding)
        }


}

private fun evaluateTemplateStatus(
    participation: Participation,
    exercise: Exercise,
    result: Result?,
    isBuilding: Boolean
): ResultTemplateStatus {
    val now = Clock.System.now()

    // Evaluate status for modeling, text and file-upload exercises
    if (exercise is ModelingExercise || exercise is TextExercise || exercise is FileUploadExercise) {
        val inDueTime = participation.isInDueTime(exercise)
        val dueDate = exercise.getDueDate(participation)
        val assessmentDueDate = exercise.assessmentDueDate

        if (inDueTime && result?.score != null) {
            // Submission is in due time of exercise and has a result with score
            return if (assessmentDueDate == null || assessmentDueDate < now) {
                ResultTemplateStatus.HAS_RESULT
            } else {
                // the assessment period is still active
                ResultTemplateStatus.SUBMITTED_WAITING_FOR_GRADING
            }
        } else if (inDueTime) {
            // Submission is in due time of exercise and doesn't have a result with score.
            return if (dueDate == null || dueDate >= now) ResultTemplateStatus.SUBMITTED
            else if (assessmentDueDate == null || assessmentDueDate >= now)
            // the due date is in the future (or there is none) => the exercise is still ongoing
                ResultTemplateStatus.SUBMITTED_WAITING_FOR_GRADING
            else
            // the due date is over, further submissions are no longer possible, no result after assessment due date
            // TODO why is this distinct from the case above? The submission can still be graded and often is.
                ResultTemplateStatus.NO_RESULT
        } else if (result?.score != null && (assessmentDueDate == null || assessmentDueDate < now)) {
            // Submission is not in due time of exercise, has a result with score and there is no assessmentDueDate for the exercise or it lies in the past.
            // TODO handle external submissions with new status "External"
            return ResultTemplateStatus.LATE;
        } else {
            // Submission is not in due time of exercise and there is actually no feedback for the submission or the feedback should not be displayed yet.
            return ResultTemplateStatus.LATE_NO_FEEDBACK;
        }
    }

    // Evaluate status for programming and quiz exercises
    if (exercise is ProgrammingExercise || exercise is QuizExercise) {
        return if (isBuilding) {
            ResultTemplateStatus.IS_BUILDING;
        } else if (result?.score != null) {
            ResultTemplateStatus.HAS_RESULT;
        } else {
            ResultTemplateStatus.NO_RESULT;
        }
    }

    return ResultTemplateStatus.NO_RESULT;
}