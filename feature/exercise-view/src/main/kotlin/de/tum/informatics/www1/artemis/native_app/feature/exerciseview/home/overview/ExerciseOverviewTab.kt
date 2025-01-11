package de.tum.informatics.www1.artemis.native_app.feature.exerciseview.home.overview

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.annotation.StringRes
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.web.WebViewState
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.ArtemisWebView
import de.tum.informatics.www1.artemis.native_app.core.ui.date.getRelativeTime
import de.tum.informatics.www1.artemis.native_app.core.ui.date.hasPassed
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseActions
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseCategoryChipRow
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseInfoChip
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseInfoChipTextHorizontalPadding
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.R
import kotlinx.datetime.Instant


@SuppressLint("SetJavaScriptEnabled")
@Composable
internal fun ExerciseOverviewTab(
    modifier: Modifier = Modifier,
    exercise: Exercise,
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
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.onPrimary
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
    isLongToolbar: Boolean
) {
    var maxWidth: Int by remember { mutableIntStateOf(0) }
    val updateMaxWidth = { new: Int -> maxWidth = new }

    val nullableDueDateTextInfo = @Composable { dueDate: Instant?, hintRes: Int ->
        if (dueDate != null) {
            DueDateTextInfo(
                dueDate = dueDate,
                hintRes = hintRes,
                maxWidth = maxWidth,
                updateMaxWidth = updateMaxWidth
            )
        }
    }

    val categoryPointsReleaseDateUi = @Composable {
        ExerciseCategoryChipRow(
            modifier = Modifier.fillMaxWidth(),
            exercise = exercise
        )

        nullableDueDateTextInfo(
            exercise.releaseDate,
            R.string.exercise_view_overview_hint_assessment_release_date
        )
    }

    val dueDateColumnUi = @Composable { contentModifier: Modifier ->
        Column(
            modifier = contentModifier,
        ) {
            nullableDueDateTextInfo(
                exercise.assessmentDueDate,
                R.string.exercise_view_overview_hint_assessment_due_date
            )

            nullableDueDateTextInfo(
                exercise.assessmentDueDate,
                R.string.exercise_view_overview_hint_assessment_due_date
            )

            ExerciseCompliantPossibleInfo(exercise)
        }
    }

    // Actual UI
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.exercise_view_overview_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(4.dp)
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
                    categoryPointsReleaseDateUi()
                }

                dueDateColumnUi(Modifier.weight(1f))
            }
        } else {
            Column(modifier = Modifier.fillMaxWidth()) {
                categoryPointsReleaseDateUi()
                dueDateColumnUi(Modifier.fillMaxWidth())
            }
        }

        HorizontalDivider(
            modifier = Modifier,
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = stringResource(R.string.exercise_view_overview_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Start
        )
    }
}

@Composable
private fun ExerciseCompliantPossibleInfo(exercise: Exercise) {
    val complaintPossible = exercise.allowComplaintsForAutomaticAssessments ?: false

    Row(
        modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            modifier = Modifier.padding(4.dp),
            text = stringResource(R.string.exercise_view_overview_hint_assessment_complaint_possible),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            modifier = Modifier.padding(4.dp),
            text = stringResource(if (complaintPossible) R.string.exercise_view_overview_hint_assessment_complaint_possible_yes else R.string.exercise_view_overview_hint_assessment_complaint_possible_no),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun DueDateTextInfo(
    dueDate: Instant,
    @StringRes hintRes: Int,
    maxWidth: Int,
    updateMaxWidth: (Int) -> Unit
) = TextInformation(
        modifier = Modifier.fillMaxWidth(),
        hintColumnWidth = maxWidth,
        hint = stringResource(id = hintRes),
        dataText = getRelativeTime(to = dueDate).toString(),
        dataColor = getDueDateColor(dueDate),
        updateHintColumnWidth = updateMaxWidth
    )

@Composable
private fun ExerciseChannelLink(
    modifier: Modifier,
    exercise: Exercise
) {
}

/**
 * Text information composable that achieves a table like layout, where the hint is the first column
 * and the data is the second column.
 */
@Composable
private fun TextInformation(
    modifier: Modifier,
    hintColumnWidth: Int,
    hint: String,
    dataText: String,
    dataColor: Color?,
    updateHintColumnWidth: (Int) -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            modifier = Modifier.layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)

                val assignedWidth = maxOf(hintColumnWidth, placeable.width)
                if (assignedWidth > hintColumnWidth) {
                    updateHintColumnWidth(assignedWidth)
                }

                layout(width = assignedWidth, height = placeable.height) {
                    placeable.placeRelative(0, 0)
                }
            }.padding(4.dp),
            text = hint,
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(modifier = Modifier.weight(1f))

        val dataModifier = Modifier.padding(4.dp)
        if (dataColor != null) {
            ExerciseInfoChip(modifier = dataModifier, color = dataColor, text = dataText)
        } else {
            Text(
                modifier = dataModifier.padding(horizontal = ExerciseInfoChipTextHorizontalPadding),
                text = dataText,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun getDueDateColor(dueDate: Instant): Color =
    if (dueDate.hasPassed()) Color.Red else Color.Green
