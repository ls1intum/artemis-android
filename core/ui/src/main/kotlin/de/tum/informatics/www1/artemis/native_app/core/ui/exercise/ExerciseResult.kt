package de.tum.informatics.www1.artemis.native_app.core.ui.exercise

import androidx.compose.foundation.layout.*
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
import java.text.DecimalFormat

private const val MIN_SCORE_GREEN = 80
private const val MIN_SCORE_ORANGE = 40

val resultSuccess: Color
    get() = Color(0xFF4CAF50)
val resultMedium: Color
    get() = Color.Yellow
val resultBad: Color
    get() = Color.Red



/**
 * Display the result of an exercise. The result is displayed in a single row.
 * For example, it can show that a result is building or which score is available.
 */
@Composable
fun ExerciseResult(
    modifier: Modifier,
    showUngradedResults: Boolean = true,
    templateStatus: ResultTemplateStatus? = LocalTemplateStatusProvider.current(),
    exercise: Exercise
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
                result = templateStatus.result,
                isLate = true,
                maxPoints = exercise.maxPoints
            )
        }
        ResultTemplateStatus.Missing, null -> {
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
        resultScore >= MIN_SCORE_GREEN -> resultSuccess
        resultScore >= MIN_SCORE_ORANGE -> resultMedium
        else -> resultBad
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
