package de.tum.informatics.www1.artemis.native_app.feature.exerciseview.home.overview

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.annotation.StringRes
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.google.accompanist.web.WebViewState
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise
import de.tum.informatics.www1.artemis.native_app.core.ui.LocalLinkOpener
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.ArtemisWebView
import de.tum.informatics.www1.artemis.native_app.core.ui.date.getRelativeTime
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseActions
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseCategoryChipRow
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseInfoChip
import de.tum.informatics.www1.artemis.native_app.core.ui.material.colors.ExerciseColors
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.humanReadableName
import kotlinx.datetime.Instant

private val exerciseInformationColumnModifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)

@SuppressLint("SetJavaScriptEnabled")
@Composable
internal fun ExerciseOverviewTab(
    modifier: Modifier = Modifier,
    exercise: Exercise,
    exerciseChannel: ChannelChat?,
    isLongToolbar: Boolean,
    webViewState: WebViewState?,
    serverUrl: String,
    authToken: String,
    setWebView: (WebView) -> Unit,
    webView: WebView?,
    actions: ExerciseActions
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
    ) {
        ParticipationStatusUi(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            exercise = exercise,
            actions = actions
        )

        // The Spacers in this Column are needed as verticalArrangement.spacedBy does lead to a gap under the ArtemisWebView
        Spacer(modifier = Modifier.height(16.dp))

        ExerciseInformation(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacings.ScreenHorizontalSpacing)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.extraSmall
                ),
            exercise = exercise,
            exerciseChannel = exerciseChannel,
            isLongToolbar = isLongToolbar
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (exercise !is QuizExercise && webViewState != null) {
            ArtemisWebView(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                webViewState = webViewState,
                webView = webView,
                serverUrl = serverUrl,
                authToken = authToken,
                adjustHeightForContent = true,
                setWebView = setWebView
            )
        } else {
            Text(
                text = stringResource(id = R.string.exercise_view_overview_problem_statement_not_available),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}


/**
 * @param isLongToolbar if the deadline information is displayed on the right side of the toolbar.
 * If false, the information is instead displayed in the column
 */
@Composable
private fun ExerciseInformation(
    modifier: Modifier,
    exercise: Exercise,
    exerciseChannel: ChannelChat?,
    isLongToolbar: Boolean
) {
    val complaintPossible = exercise.allowComplaintsForAutomaticAssessments ?: false

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

    val leftColumnUi = @Composable {
        nullableDateTextInfo(
            exercise.releaseDate,
            R.string.exercise_view_overview_hint_assessment_release_date,
            false
        )

        // We always want to display a submission due date
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

    val rightColumnUi = @Composable { contentModifier: Modifier ->
        val complaintPossibleText =
            if (complaintPossible) R.string.exercise_view_overview_hint_assessment_complaint_possible_yes else R.string.exercise_view_overview_hint_assessment_complaint_possible_no

        Column(
            modifier = contentModifier,
        ) {
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

    // Actual UI
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.exercise_view_overview_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = exerciseInformationColumnModifier
                .fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        HorizontalDivider(
            modifier = Modifier,
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.primary
        )

        // Here we make the distinction in the layout between long toolbar and short toolbar
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
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.primary
        )

        ExerciseChannelLink(
            modifier = Modifier.fillMaxWidth(),
            exercise = exercise,
            exerciseChannel = exerciseChannel
        )
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
private fun ExerciseChannelLink(
    modifier: Modifier,
    exercise: Exercise,
    exerciseChannel: ChannelChat?
) {
    val localLinkOpener = LocalLinkOpener.current

    Row(
        modifier = modifier.padding(start = 8.dp).padding(vertical = 4.dp),
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
                if (exerciseChannel != null && courseId != null) {
                    localLinkOpener.openLink("artemis://courses/$courseId/messages?conversationId=${exerciseChannel.id}")
                }
            },
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (exerciseChannel != null) {
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
