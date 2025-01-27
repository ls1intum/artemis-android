package de.tum.informatics.www1.artemis.native_app.core.ui.exercise

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.core.ui.R
import de.tum.informatics.www1.artemis.native_app.core.ui.date.getRelativeTime
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.util.ExerciseResultUtil
import de.tum.informatics.www1.artemis.native_app.core.ui.material.colors.ExerciseColors
import java.text.DecimalFormat

private const val MIN_SCORE_GREEN = 80
private const val MIN_SCORE_ORANGE = 40

/**
 * Display the result of an exercise. The result is displayed in a single row.
 * For example, it can show that a result is building or which score is available.
 */
@Composable
fun ExerciseResult(
    modifier: Modifier,
    showUngradedResults: Boolean = false,
    templateStatus: ResultTemplateStatus? = LocalTemplateStatusProvider.current(),
    exercise: Exercise,
    showLargeIcon: Boolean = false,
    showPoints: Boolean = false
) {
    when (templateStatus) {
        ResultTemplateStatus.IsBuilding -> {
            StatusIsBuilding(modifier = modifier.height(IntrinsicSize.Min))
        }

        is ResultTemplateStatus.HasResult -> {
            StatusHasResult(
                modifier = modifier.height(IntrinsicSize.Min),
                result = templateStatus.result,
                isLate = false,
                maxPoints = exercise.maxPoints,
                exercise = exercise,
                showLargeIcon = showLargeIcon,
                showPoints = showPoints
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
                result = templateStatus.result,
                isLate = true,
                maxPoints = exercise.maxPoints,
                exercise = exercise,
                showLargeIcon = showLargeIcon,
                showPoints = showPoints
            )
        }

        ResultTemplateStatus.Missing, null -> {
            TextStatus(
                modifier = modifier,
                text = stringResource(id = R.string.exercise_result_missing)
            )
        }
    }
}

private val statusTextStyle: TextStyle
    @Composable get() = MaterialTheme.typography.bodyMedium


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
    result: Result,
    isLate: Boolean,
    maxPoints: Float?,
    exercise: Exercise,
    showLargeIcon: Boolean = false,
    showPoints: Boolean = false
) {
    val resultScore = result.score ?: 0f
    val points = DecimalFormat.getInstance().format(resultScore / 100f * (exercise.maxPoints ?: 0f))

    val icon = if (resultScore < MIN_SCORE_GREEN) {
        Icons.Default.Cancel
    } else {
        Icons.Default.CheckCircle
    }

    val textAndIconColor = when {
        resultScore >= MIN_SCORE_GREEN -> ExerciseColors.Result.success
        resultScore >= MIN_SCORE_ORANGE -> ExerciseColors.Result.medium
        else -> ExerciseColors.Result.bad
    }

    val context = LocalContext.current

    val completionDate = result.completionDate

    //The relative time to this result being finished.
    val relativeTime = if (completionDate == null) null else getRelativeTime(to = completionDate)

    val text = remember(resultScore, maxPoints, relativeTime) {
        if (maxPoints == null || relativeTime == null)
            return@remember context.getString(R.string.exercise_result_has_result_score_unknown)

        val formattedPercentage = DecimalFormat("0.#%").format(resultScore / 100f)

        val scoreString = ExerciseResultUtil.resolveScoreString(
            exercise,
            formattedPercentage,
            result,
            points,
            context,
            showPoints
        )

        context.getString(
            if (isLate) R.string.exercise_result_has_result_score_late_with_date
            else R.string.exercise_result_has_result_score_with_date,
            scoreString,
            relativeTime
        )
    }

    IconTextStatus(
        modifier = modifier.height(IntrinsicSize.Min),
        icon = icon,
        text = text,
        iconColor = textAndIconColor,
        textColor = textAndIconColor,
        showLargeIcon = showLargeIcon
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
    textStyle: TextStyle = statusTextStyle,
    showLargeIcon: Boolean = false
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            tint = iconColor,
            modifier = Modifier.apply {
                if (showLargeIcon)
                    fillMaxHeight()
                        .aspectRatio(1f, matchHeightConstraintsFirst = true)
                else height(16.dp)
            },
            contentDescription = null
        )

        Text(
            text = text,
            color = textColor,
            style = textStyle,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}
