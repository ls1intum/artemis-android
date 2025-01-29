package de.tum.informatics.www1.artemis.native_app.feature.exerciseview.home.overview

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.ui.LocalLinkOpener
import de.tum.informatics.www1.artemis.native_app.core.ui.date.getRelativeTime
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseCategoryChipRow
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseInfoChip
import de.tum.informatics.www1.artemis.native_app.core.ui.material.colors.ExerciseColors
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.humanReadableName
import kotlinx.datetime.Instant

private val exerciseInformationColumnModifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
val informationTableThickness = 1.dp

/**
 * @param isLongToolbar if the deadline information is displayed on the right side of the toolbar.
 * If false, the information is instead displayed in the column
 */
@Composable
fun ExerciseInformation(
    modifier: Modifier,
    exercise: Exercise,
    exerciseChannel: ChannelChat?,
    isLongToolbar: Boolean
) {
    Column(modifier = modifier) {
        ExerciseInformationHeader()

        ExerciseInformationContent(
            exercise = exercise,
            isLongToolbar = isLongToolbar
        )

        exerciseChannel?.let {
            ExerciseChannelLink(
                modifier = Modifier.fillMaxWidth(),
                exercise = exercise,
                exerciseChannel = exerciseChannel
            )
        }
    }
}

@Composable
private fun ExerciseInformationHeader() {
    Column {
        Text(
            text = stringResource(R.string.exercise_view_overview_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = exerciseInformationColumnModifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        HorizontalDivider(
            modifier = Modifier,
            thickness = informationTableThickness,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ExerciseInformationContent(
    exercise: Exercise,
    isLongToolbar: Boolean
) {
    val leftColumnUi = @Composable {
        ExerciseLeftColumn(exercise)
    }

    val rightColumnUi = @Composable { contentModifier: Modifier ->
        ExerciseRightColumn(
            modifier = contentModifier,
            exercise = exercise
        )
    }

    if (isLongToolbar) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(modifier = Modifier.weight(1f)) {
                leftColumnUi()
            }

            Spacer(modifier = Modifier.width(16.dp))

            rightColumnUi(Modifier.weight(1f))
        }
    } else {
        Column(modifier = Modifier.fillMaxWidth()) {
            leftColumnUi()
            rightColumnUi(Modifier.fillMaxWidth())
        }
    }

    HorizontalDivider(
        modifier = Modifier,
        thickness = informationTableThickness,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun ExerciseLeftColumn(exercise: Exercise) {
    val nullableDateTextInfo = @Composable { dueDate: Instant?, hintRes: Int, showColor: Boolean ->
        if (dueDate != null) {
            DateInfoText(
                modifier = Modifier.fillMaxWidth(),
                dueDate = getRelativeTime(to = dueDate, showDateAndTime = true),
                hintRes = hintRes,
                dataColor = if (showColor) ExerciseColors.getDueDateColor(dueDate) else null
            )
        }
    }

    nullableDateTextInfo(
        exercise.releaseDate,
        R.string.exercise_view_overview_hint_assessment_release_date,
        false
    )

    if (exercise.dueDate == null) {
        TextAndValueRow(
            modifier = Modifier.fillMaxWidth(),
            exercise = exercise,
            hintRes = R.string.exercise_view_overview_hint_submission_due_date,
            value = R.string.exercise_view_overview_no_submission_due_date
        )
    } else {
        nullableDateTextInfo(
            exercise.dueDate,
            R.string.exercise_view_overview_hint_submission_due_date,
            true
        )
    }

    nullableDateTextInfo(
        exercise.assessmentDueDate,
        R.string.exercise_view_overview_hint_assessment_due_date,
        true
    )
}

@Composable
private fun ExerciseRightColumn(
    modifier: Modifier,
    exercise: Exercise
) {
    val complaintPossible = exercise.allowComplaintsForAutomaticAssessments ?: false
    val complaintPossibleText =
        if (complaintPossible) R.string.exercise_view_overview_hint_assessment_complaint_possible_yes else R.string.exercise_view_overview_hint_assessment_complaint_possible_no

    Column(modifier = modifier) {
        TextAndValueRow(
            modifier = Modifier.fillMaxWidth(),
            exercise = exercise,
            hintRes = R.string.exercise_view_overview_hint_assessment_complaint_possible,
            value = complaintPossibleText
        )

        if (exercise.includedInOverallScore != Exercise.IncludedInOverallScore.INCLUDED_COMPLETELY) {
            val (text, color) = when (exercise.includedInOverallScore) {
                Exercise.IncludedInOverallScore.INCLUDED_AS_BONUS -> Pair(
                    R.string.exercise_view_overview_hint_exercise_type_bonus,
                    ExerciseColors.Type.bonus
                )
                Exercise.IncludedInOverallScore.NOT_INCLUDED -> Pair(
                    R.string.exercise_view_overview_hint_exercise_type_optional,
                    ExerciseColors.Type.notIncluded
                )
                else -> Pair(R.string.exercise_type_unknown, null)
            }

            TextAndValueRow(
                modifier = Modifier.fillMaxWidth(),
                exercise = exercise,
                hintRes = R.string.exercise_view_overview_hint_exercise_type,
                value = text,
                dataColor = color
            )
        }

        if (exercise.difficulty != null) {
            val (text, color) = when (exercise.difficulty) {
                Exercise.Difficulty.EASY -> Pair(
                    R.string.exercise_view_overview_hint_difficulty_easy,
                    ExerciseColors.Difficulty.easy
                )
                Exercise.Difficulty.MEDIUM -> Pair(
                    R.string.exercise_view_overview_hint_difficulty_medium,
                    ExerciseColors.Difficulty.medium
                )
                Exercise.Difficulty.HARD -> Pair(
                    R.string.exercise_view_overview_hint_difficulty_hard,
                    ExerciseColors.Difficulty.hard
                )
                else -> Pair(R.string.exercise_type_unknown, null)
            }

            TextAndValueRow(
                modifier = Modifier.fillMaxWidth(),
                exercise = exercise,
                hintRes = R.string.exercise_view_overview_hint_difficulty,
                value = text,
                dataColor = color
            )
        }

        if (exercise.categories.isNotEmpty()) {
            TextAndValueRow(
                modifier = Modifier.fillMaxWidth(),
                exercise = exercise,
                hintRes = R.string.exercise_view_overview_hint_exercise_categories
            )
        }
    }
}

@Composable
private fun TextAndValueRow(
    modifier: Modifier,
    exercise: Exercise,
    @StringRes hintRes: Int,
    @StringRes value: Int? = null,
    dataColor: Color? = null,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            modifier = exerciseInformationColumnModifier,
            text = stringResource(hintRes),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.weight(1f))

        if (value == null) {
            ExerciseCategoryChipRow(
                modifier = exerciseInformationColumnModifier,
                exercise = exercise,
                includeType = false
            )
            return
        }

        if (dataColor != null) {
            ExerciseInfoChip(
                modifier = exerciseInformationColumnModifier,
                color = dataColor,
                text = stringResource(value)
            )
        } else {
            Text(
                modifier = exerciseInformationColumnModifier,
                text = stringResource(value),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun DateInfoText(
    modifier: Modifier,
    @StringRes hintRes: Int,
    dueDate: CharSequence,
    dataColor: Color?
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            modifier = exerciseInformationColumnModifier,
            text = stringResource(hintRes),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            modifier = exerciseInformationColumnModifier,
            text = dueDate.toString(),
            color = dataColor ?: MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ExerciseChannelLink(
    modifier: Modifier,
    exercise: Exercise,
    exerciseChannel: ChannelChat
) {
    val localLinkOpener = LocalLinkOpener.current

    Row(
        modifier = modifier
            .padding(start = 8.dp)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            modifier = Modifier,
            text = stringResource(R.string.exercise_view_overview_hint_exercise_communication),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.clickable {
                val courseId = exercise.course?.id
                courseId?.let {
                    localLinkOpener.openLink("artemis://courses/$courseId/messages?conversationId=${exerciseChannel.id}")
                }
            },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier,
                text = exerciseChannel.humanReadableName.removePrefix("exercise-"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Icon(
                modifier = Modifier,
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}