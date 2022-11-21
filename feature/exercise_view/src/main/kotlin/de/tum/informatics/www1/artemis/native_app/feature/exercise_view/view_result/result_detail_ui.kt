package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.view_result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.ProgrammingExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.ExerciseViewModel
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.R
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.round

@Composable
internal fun ResultDetailUi(
    modifier: Modifier,
    exercise: Exercise,
    latestResult: Result,
    feedbackItems: List<ExerciseViewModel.FeedbackItem>,
    latestIndividualDueDate: Instant?,
    forceShowTestDetails: Boolean = false
) {
    val showTestDetails =
        (exercise is ProgrammingExercise && exercise.showTestNamesToStudents == true) || forceShowTestDetails
    val showMissingAutomaticFeedbackInformation = remember(latestIndividualDueDate) {
        if (latestIndividualDueDate == null) {
            false
        } else {
            Clock.System.now() < latestIndividualDueDate
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (showMissingAutomaticFeedbackInformation) {
            MissingFeedbackInformation(
                modifier = Modifier.fillMaxWidth(),
                latestIndividualDueDate = latestIndividualDueDate
            )

            Divider()
        }

        if (latestResult.isPreliminary) {
            ResultIsPreliminaryWarning(
                modifier = Modifier.fillMaxWidth(),
                assessmentType = exercise.assessmentType
            )

            Divider()
        }

        ScoreResultsCard(
            modifier = Modifier.fillMaxWidth(),
            exercise = exercise,
            feedbackItems = feedbackItems
        )
    }
}

@Composable
private fun MissingFeedbackInformation(modifier: Modifier, latestIndividualDueDate: Instant?) {
    val formattedDate = remember(latestIndividualDueDate) {
        if (latestIndividualDueDate == null) {
            ""
        } else {
            SimpleDateFormat.getDateTimeInstance(
                SimpleDateFormat.MEDIUM,
                SimpleDateFormat.SHORT
            )
                .format(Date.from(latestIndividualDueDate.toJavaInstant()))
        }
    }

    Text(
        modifier = modifier,
        text = stringResource(
            id = R.string.result_view_automatic_feedback_missing,
            formattedDate
        ),
        style = MaterialTheme.typography.bodyMedium,
        fontStyle = FontStyle.Italic
    )
}

@Composable
private fun ResultIsPreliminaryWarning(
    modifier: Modifier,
    assessmentType: Exercise.AssessmentType?
) {
    val text = stringResource(
        id = if (assessmentType == Exercise.AssessmentType.AUTOMATIC) {
            R.string.result_view_preliminary_result_semi_automatic
        } else R.string.result_view_preliminary_result_automatic
    )

    Text(
        modifier = modifier,
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun ScoreResultsCard(
    modifier: Modifier,
    exercise: Exercise,
    feedbackItems: List<ExerciseViewModel.FeedbackItem>
) {
    val chartValues = remember(feedbackItems) {
        val maxPoints = exercise.maxPoints ?: 0f
        val maxPointsWithBonus = maxPoints + (exercise.bonusPoints ?: 0f)

        val positiveCredits =
            feedbackItems
                .filter { it.type != ExerciseViewModel.FeedbackItemType.Test && it.credits != null && it.credits > 0 }
                .sumOf { it.creditsOrZero }

        val maxCodeIssueCredits =
            if (exercise is ProgrammingExercise) {
                val maxStaticCodeAnalysisPenalty = exercise.maxStaticCodeAnalysisPenalty

                if (exercise.staticCodeAnalysisEnabled && maxStaticCodeAnalysisPenalty != null) {
                    maxPoints.toDouble() * maxStaticCodeAnalysisPenalty.toDouble() / 100.0
                } else Double.MAX_VALUE
            } else Double.MAX_VALUE

        val codeIssueCredits =
            -feedbackItems
                .filter { it.type == ExerciseViewModel.FeedbackItemType.Issue }
                .sumOf { it.actualCreditsOrZero }
                .coerceAtMost(maxCodeIssueCredits)
        val codeIssuePenalties =
            feedbackItems
                .filter { it.type == ExerciseViewModel.FeedbackItemType.Issue }
                .sumOf { it.creditsOrZero }
        val negativeCredits =
            -feedbackItems
                .filter { it.type == ExerciseViewModel.FeedbackItemType.Issue && it.credits != null && it.credits < 0 }
                .sumOf { it.creditsOrZero }

        val testCaseCredits =
            feedbackItems
                .filter { it.type == ExerciseViewModel.FeedbackItemType.Test }
                .sumOf { it.creditsOrZero }
                .coerceAtMost(maxPointsWithBonus.toDouble())

        val accuracy = exercise.course?.accuracyOfScores?.toDouble() ?: 1.0

        val appliedNegativePoints = (codeIssueCredits + negativeCredits).rounded(accuracy)
        val receivedNegativePoints = (codeIssuePenalties + negativeCredits).rounded(accuracy)
        val positivePoints = (testCaseCredits + positiveCredits).rounded(accuracy)

        ChartValues(
            positivePoints,
            appliedNegativePoints,
            receivedNegativePoints,
            maxPoints,
            maxPointsWithBonus
        )
    }

    Row(modifier = modifier) {
        val scoreResultModifier = Modifier.weight(1f)
        ScoreResult(
            modifier = scoreResultModifier,
            title = stringResource(id = R.string.result_view_feedback_overview_category_correct),
            points = chartValues.positivePoints,
            percentage = chartValues.positivePoints / chartValues.maxPoints
        )

        ScoreResult(
            modifier = scoreResultModifier,
            title = stringResource(id = R.string.result_view_feedback_overview_category_warning),
            points = chartValues.appliedNegativePoints,
            percentage = chartValues.appliedNegativePoints / chartValues.maxPoints
        )

        ScoreResult(
            modifier = scoreResultModifier,
            title = stringResource(id = R.string.result_view_feedback_overview_category_wrong),
            points = chartValues.receivedNegativePoints,
            percentage = chartValues.receivedNegativePoints / chartValues.maxPoints
        )
    }
}

private val pointDecimalFormat = DecimalFormat("00.00")
private val percentageFormat = DecimalFormat.getPercentInstance()

@Composable
private fun ScoreResult(modifier: Modifier, title: String, points: Double, percentage: Double) {
    val pointText = remember(points) {
        pointDecimalFormat.format(points)
    }

    val percentageText = remember(percentage) {
        percentageFormat.format(percentage)
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = pointText,
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = percentageText,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Rounds the value to the next multiple of accuracy.
 */
private fun Double.rounded(accuracy: Double): Double {
    return round(this * (1.0 / accuracy)) * accuracy
}

private data class ChartValues(
    val positivePoints: Double,
    val appliedNegativePoints: Double,
    val receivedNegativePoints: Double,
    val maxPoints: Float,
    val maxPointsWithBonus: Float
)