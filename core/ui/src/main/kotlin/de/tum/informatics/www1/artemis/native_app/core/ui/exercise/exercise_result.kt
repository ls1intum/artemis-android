package de.tum.informatics.www1.artemis.native_app.core.ui.exercise

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.*
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.isInDueTime
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.InstructorSubmission
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.TestSubmission
import de.tum.informatics.www1.artemis.native_app.core.ui.R
import de.tum.informatics.www1.artemis.native_app.core.ui.date.getRelativeTime
import de.tum.informatics.www1.artemis.native_app.core.websocket.LiveParticipationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.koin.androidx.compose.get
import java.text.DecimalFormat

private const val MIN_SCORE_GREEN = 80
private const val MIN_SCORE_ORANGE = 40

val ColorScheme.resultSuccess: Color
    get() = Color.Green
val ColorScheme.resultMedium: Color
    get() = Color.Yellow
val ColorScheme.resultBad: Color
    get() = Color.Red

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
@Composable
fun computeTemplateStatus(
    exercise: Exercise,
    participation: Participation,
    result: Result?,
    showUngradedResults: Boolean,
    personal: Boolean
): Flow<ResultTemplateStatus> {
    val service: LiveParticipationService = get()

    val participationId = participation.id ?: 0
    val exerciseId = exercise.id ?: 0

    val isBuildingFlow = remember(participationId, exerciseId, personal, showUngradedResults) {
        service
            .getLatestPendingSubmissionByParticipationIdFlow(participationId, exerciseId, personal)
            .filter { submissionData ->
                val shouldUpdateBasedOnData: Boolean = when (submissionData) {
                    is LiveParticipationService.ProgrammingSubmissionStateData.IsBuildingPendingSubmission -> {
                        val submission = submissionData.submission

                        val submissionDate = submission.submissionDate ?: Instant.fromEpochSeconds(0L)
                        val dueDate = exercise.getDueDate(participation) ?: Instant.fromEpochSeconds(0L)

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
    }

    val chosenResult: Result? = remember(participation, result) {
        result
            ?: participation.results.orEmpty()
                .maxByOrNull { it.completionDate ?: Instant.fromEpochSeconds(0L) }
    }

    return remember(isBuildingFlow, chosenResult) {
        isBuildingFlow.map { isBuilding ->
            evaluateTemplateStatus(participation, exercise, chosenResult, isBuilding)
        }
    }
}

/**
 * Display the result of an exercise. The result is displayed in a single row.
 * For example, it can show that a result is building or which score is available.
 */
@Composable
fun ExerciseResult(
    modifier: Modifier,
    showUngradedResults: Boolean = true,
    templateStatus: ResultTemplateStatus,
    exercise: Exercise
) {
    when (templateStatus) {
        ResultTemplateStatus.IsBuilding -> {
            StatusIsBuilding(modifier = modifier.height(IntrinsicSize.Min))
        }
        is ResultTemplateStatus.HasResult -> {
            StatusHasResult(
                modifier = modifier.height(IntrinsicSize.Min),
                showIcon = true,
                result = templateStatus.result,
                isLate = false,
                maxPoints = exercise.maxPoints
            )
        }
        ResultTemplateStatus.NoResult -> {
            StatusNoResult(modifier = modifier, showUngradedResults = showUngradedResults)
        }
        ResultTemplateStatus.Submitted -> {
            TextStatus(
                modifier = modifier,
                text = stringResource(id = R.string.exercise_result_submitted)
            )
        }
        ResultTemplateStatus.SubmittedWaitingForGrading -> {
            TextStatus(
                modifier = modifier,
                text = stringResource(id = R.string.exercise_result_submitted_waiting_for_grading)
            )
        }
        ResultTemplateStatus.LateNoFeedback -> {
            TextStatus(
                modifier = modifier,
                text = stringResource(id = R.string.exercise_result_late_submission)
            )
        }
        is ResultTemplateStatus.Late -> {
            StatusHasResult(
                modifier = modifier,
                showIcon = true,
                result = templateStatus.result,
                isLate = true,
                maxPoints = exercise.maxPoints
            )
        }
        ResultTemplateStatus.Missing -> {
            // TODO
        }
    }
}

private val statusTextStyle: TextStyle
    @Composable get() = MaterialTheme.typography.labelMedium


@Composable
private fun StatusIsBuilding(modifier: Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        LinearProgressIndicator(
            modifier = Modifier
                .weight(2f)
                .align(Alignment.CenterVertically)
        )

        Text(
            text = stringResource(id = R.string.exercise_result_is_building),
            style = statusTextStyle,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .weight(8f)
        )
    }
}

@Composable
private fun StatusHasResult(
    modifier: Modifier,
    showIcon: Boolean,
    result: Result,
    isLate: Boolean,
    maxPoints: Float?
) {
    val resultScore = result.score ?: 0f

    val icon = if (resultScore < MIN_SCORE_GREEN) {
        Icons.Default.Cancel
    } else {
        Icons.Default.CheckCircle
    }

    val textAndIconColor = when {
        resultScore >= MIN_SCORE_GREEN -> MaterialTheme.colorScheme.resultSuccess
        resultScore >= MIN_SCORE_ORANGE -> MaterialTheme.colorScheme.resultMedium
        else -> MaterialTheme.colorScheme.resultBad
    }

    val context = LocalContext.current

    val completionDate = result.completionDate

    //The relative time to this result being finished.
    val relativeTime = if (completionDate == null) null else getRelativeTime(to = completionDate)

    val text = remember(resultScore, maxPoints, relativeTime) {
        if (maxPoints == null || relativeTime == null)
            return@remember context.getString(R.string.exercise_result_has_result_score_unknown)

        val formattedPercentage = DecimalFormat.getPercentInstance().format(resultScore / 100f)

        context.getString(
            if (isLate) R.string.exercise_result_has_result_score_late
            else R.string.exercise_result_has_result_score,
            formattedPercentage,
            relativeTime
        )
    }

    IconTextStatus(
        modifier = modifier.height(IntrinsicSize.Min),
        icon = icon,
        text = text,
        iconColor = textAndIconColor,
        textColor = textAndIconColor
    )
}

@Composable
private fun StatusNoResult(modifier: Modifier, showUngradedResults: Boolean) {
    TextStatus(
        modifier = modifier,
        text = stringResource(id = if (showUngradedResults) R.string.exercise_result_no_result else R.string.exercise_result_no_graded_result)
    )
}

/**
 * Template if the status only requires a text field
 */
@Composable
private fun TextStatus(
    modifier: Modifier,
    text: String,
    textColor: Color = Color.Unspecified,
    textStyle: TextStyle = statusTextStyle
) {
    Text(
        text = text,
        modifier = modifier,
        color = textColor,
        style = textStyle
    )
}

/**
 * Template for a status that has an icon and a text in a row
 */
@Composable
private fun IconTextStatus(
    modifier: Modifier,
    icon: ImageVector,
    text: String,
    iconColor: Color = Color.Unspecified,
    textColor: Color = Color.Unspecified,
    textStyle: TextStyle = statusTextStyle
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = text,
            color = textColor,
            style = textStyle,
            modifier = Modifier.align(Alignment.CenterVertically)
        )

        Icon(
            imageVector = icon,
            tint = iconColor,
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f, matchHeightConstraintsFirst = true),
            contentDescription = null
        )
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