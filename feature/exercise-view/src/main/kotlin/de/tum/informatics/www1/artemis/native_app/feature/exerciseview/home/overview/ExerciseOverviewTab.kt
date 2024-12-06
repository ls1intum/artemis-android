package de.tum.informatics.www1.artemis.native_app.feature.exerciseview.home.overview

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
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
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.currentUserPoints
import de.tum.informatics.www1.artemis.native_app.core.ui.date.getRelativeTime
import de.tum.informatics.www1.artemis.native_app.core.ui.date.hasPassed
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseActions
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseCategoryChipRow
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseInfoChip
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseInfoChipTextHorizontalPadding
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExercisePointsDecimalFormat
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.ArtemisWebView
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
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ExerciseInformation(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp)
                .border(
                    width = 2.dp,
                    color = Color.Black,
                    shape = MaterialTheme.shapes.extraSmall
                ),
            exercise = exercise,
            isLongToolbar = isLongToolbar
        )

        ParticipationStatusUi(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            exercise = exercise,
            actions = actions
        )

        if (exercise !is QuizExercise && webViewState != null) {
            ArtemisWebView(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                webViewState = webViewState,
                webView = webView,
                serverUrl = serverUrl,
                authToken = authToken,
                setWebView = setWebView
            )
        } else {
            Text(
                text = stringResource(id = R.string.exercise_view_overview_problem_statement_not_available),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                color = Color.Gray
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

    val nullableDueDateTextInfo = @Composable { dueDate: Instant?, hintRes: @receiver:StringRes Int ->
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

        ExercisePointInfo(exercise)

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
    Column(
        modifier = modifier.padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.exercise_view_overview_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(bottom = 1.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Start
        )
        Divider(
            color = Color.Black,
            thickness = 2.dp,
            modifier = Modifier.padding(vertical = 0.dp)
        )

        // Here we make the distinction in the layout between long toolbar and short toolbar
        if (isLongToolbar) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    categoryPointsReleaseDateUi()
                }

                dueDateColumnUi(
                    Modifier
                        .width(IntrinsicSize.Max)
                        .align(Alignment.Bottom)
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxWidth()) {
                categoryPointsReleaseDateUi()
                dueDateColumnUi(Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun ExerciseCompliantPossibleInfo(exercise: Exercise) {
    val complaintPossible = exercise.allowComplaintsForAutomaticAssessments ?: false
    val complaintPossibleText = stringResource(
        R.string.exercise_view_overview_hint_assessment_complaint_possible,
        stringResource(if (complaintPossible) R.string.exercise_view_overview_hint_assessment_complaint_possible_yes else R.string.exercise_view_overview_hint_assessment_complaint_possible_no)
    )

    Text(
        modifier = Modifier.padding(bottom = 4.dp),
        text = complaintPossibleText,
        style = MaterialTheme.typography.bodyLarge
    )
}

@Composable
private fun ExercisePointInfo(exercise: Exercise) {
    val currentUserPoints = exercise.currentUserPoints?.let(ExercisePointsDecimalFormat::format)
    val maxPoints = exercise.maxPoints?.let(ExercisePointsDecimalFormat::format)

    val pointsHintText = when {
        currentUserPoints != null && maxPoints != null -> stringResource(
            id = R.string.exercise_view_overview_points_reached,
            currentUserPoints,
            maxPoints
        )

        maxPoints != null -> stringResource(
            id = R.string.exercise_view_overview_points_max,
            maxPoints
        )

        else -> stringResource(id = R.string.exercise_view_overview_points_none)
    }

    Text(
        modifier = Modifier.padding(bottom = 4.dp),
        text = pointsHintText,
        style = MaterialTheme.typography.bodyLarge
    )
}

@Composable
private fun DueDateTextInfo(
    dueDate: Instant,
    @StringRes hintRes: Int,
    maxWidth: Int,
    updateMaxWidth: (Int) -> Unit
) = TextInformation(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 1.dp),
        hintColumnWidth = maxWidth,
        hint = stringResource(id = hintRes),
        dataText = getRelativeTime(to = dueDate).toString(),
        dataColor = getDueDateColor(dueDate),
        updateHintColumnWidth = updateMaxWidth
    )

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
            },
            text = hint,
            style = MaterialTheme.typography.bodyLarge,
        )

        val dataModifier = Modifier

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
