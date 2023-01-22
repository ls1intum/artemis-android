package de.tum.informatics.www1.artemis.native_app.core.ui.exercise

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.*
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.isInDueTime
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.InstructorSubmission
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.TestSubmission
import de.tum.informatics.www1.artemis.native_app.core.websocket.LiveParticipationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.koin.androidx.compose.get

/**
 * Enumeration object representing the possible options that
 * the status of the result's template can be in.
 */
sealed class ResultTemplateStatus {

    sealed class WithResult(val result: Result) : ResultTemplateStatus()

    /**
     * An automatic result is currently being generated and should be available soon.
     * This is currently only relevant for programming exercises.
     */
    object IsBuilding : ResultTemplateStatus()

    /**
     * A regular, finished result is available.
     * Can be rated (counts toward the score) or not rated (after the deadline for practice).
     */
    class HasResult(result: Result) : WithResult(result)

    /**
     * There is no result or submission status that could be shown, e.g. because the student just started with the exercise.
     */
    object NoResult : ResultTemplateStatus()

    /**
     * Submitted and the student can still continue to submit.
     */
    object Submitted : ResultTemplateStatus()

    /**
     * Submitted and the student can no longer submit, but a result is not yet available.
     */
    object SubmittedWaitingForGrading : ResultTemplateStatus()

    /**
     * The student started the exercise but submitted too late.
     * Feedback is not yet available, and a future result will not count toward the score.
     */
    object LateNoFeedback : ResultTemplateStatus()

    /**
     * The student started the exercise and submitted too late, but feedback is available.
     */
    class Late(result: Result) : WithResult(result)

    /**
     * No latest result available, e.g. because building took too long and the webapp did not receive it in time.
     * This is a distinct state because we want the student to know about this problematic state
     * and not confuse them by showing a previous result that does not match the latest submission.
     */
    object Missing : ResultTemplateStatus()
}

/**
 * Compute the result template status and return it as a flow.
 * The flow emits whenever the template status changes.
 */
fun computeTemplateStatus(
    service: LiveParticipationService,
    exercise: Exercise,
    participation: Participation,
    result: Result?,
    showUngradedResults: Boolean,
    personal: Boolean
): Flow<ResultTemplateStatus> {
    val participationId = participation.id ?: 0
    val exerciseId = exercise.id

    val isBuildingFlow =
        service
            .getLatestPendingSubmissionByParticipationIdFlow(participationId, exerciseId, personal)
            .filter { submissionData ->
                val shouldUpdateBasedOnData: Boolean = when (submissionData) {
                    is LiveParticipationService.ProgrammingSubmissionStateData.IsBuildingPendingSubmission -> {
                        val submission = submissionData.submission

                        val submissionDate =
                            submission.submissionDate ?: Instant.fromEpochSeconds(0L)
                        val dueDate =
                            exercise.getDueDate(participation) ?: Instant.fromEpochSeconds(0L)

                        submission is InstructorSubmission
                                || submission is TestSubmission
                                || submissionDate < dueDate
                    }
                    is LiveParticipationService.ProgrammingSubmissionStateData.FailedSubmission,
                    is LiveParticipationService.ProgrammingSubmissionStateData.NoPendingSubmission,
                    null -> true
                }

                showUngradedResults
                        || exercise.dueDate == null
                        || shouldUpdateBasedOnData
            }
            .map { submissionData ->
                submissionData is LiveParticipationService.ProgrammingSubmissionStateData.IsBuildingPendingSubmission
            }
            .onStart { emit(false) }


    val chosenResult: Result? =
        result
            ?: participation.results.orEmpty()
                .maxByOrNull { it.completionDate ?: Instant.fromEpochSeconds(0L) }


    return isBuildingFlow.map { isBuilding ->
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
                ResultTemplateStatus.HasResult(result)
            } else {
                // the assessment period is still active
                ResultTemplateStatus.SubmittedWaitingForGrading
            }
        } else if (inDueTime) {
            // Submission is in due time of exercise and doesn't have a result with score.
            return if (dueDate == null || dueDate >= now) ResultTemplateStatus.Submitted
            else if (assessmentDueDate == null || assessmentDueDate >= now)
            // the due date is in the future (or there is none) => the exercise is still ongoing
                ResultTemplateStatus.SubmittedWaitingForGrading
            else
            // the due date is over, further submissions are no longer possible, no result after assessment due date
            // TODO why is this distinct from the case above? The submission can still be graded and often is.
                ResultTemplateStatus.NoResult
        } else if (result?.score != null && (assessmentDueDate == null || assessmentDueDate < now)) {
            // Submission is not in due time of exercise, has a result with score and there is no assessmentDueDate for the exercise or it lies in the past.
            // TODO handle external submissions with new status "External"
            return ResultTemplateStatus.Late(result)
        } else {
            // Submission is not in due time of exercise and there is actually no feedback for the submission or the feedback should not be displayed yet.
            return ResultTemplateStatus.LateNoFeedback
        }
    }

    // Evaluate status for programming and quiz exercises
    if (exercise is ProgrammingExercise || exercise is QuizExercise) {
        return if (isBuilding) {
            ResultTemplateStatus.IsBuilding
        } else if (result?.score != null) {
            ResultTemplateStatus.HasResult(result)
        } else {
            ResultTemplateStatus.NoResult
        }
    }

    return ResultTemplateStatus.NoResult
}