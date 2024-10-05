package de.tum.informatics.www1.artemis.native_app.core.ui.exercise

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.ui.R
import de.tum.informatics.www1.artemis.native_app.core.ui.date.getRelativeTime
import de.tum.informatics.www1.artemis.native_app.core.ui.getWindowSizeClass

private val difficultyHardColor: Color
    @Composable get() = Color(0xffdc3545)
private val difficultyMediumColor: Color
    @Composable get() = Color(0xffffc107)
private val difficultyEasyColor: Color
    @Composable get() = Color(0xff28a745)

/**
 * Display a single exercise.
 * The exercise is displayed in a card with an icon specific to the exercise type.
 */
@Composable
fun ExerciseListItem(
    modifier: Modifier,
    exercise: Exercise,
    displayActionButtons: Boolean = getWindowSizeClass().widthSizeClass >= WindowWidthSizeClass.Expanded,
    onClickExercise: () -> Unit,
    exerciseActions: ExerciseActions
) {
    Card(
        modifier = modifier,
        onClick = onClickExercise
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            if (exercise.difficulty != null) {
                exercise.difficulty?.let { DifficultyRectangle(modifier = Modifier, difficulty = it)}
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExerciseTypeIcon(
                        modifier = Modifier.size(40.dp)
                            .padding(horizontal = 8.dp),
                        exercise = exercise
                    )

                    //Displays the title of the exercise
                    Text(
                        text = exercise.title.orEmpty(),
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                ExerciseDataText(
                    modifier = Modifier
                        .padding(horizontal = 8.dp),
                    exercise = exercise,
                    displayActionButtons = displayActionButtons,
                    exerciseActions = exerciseActions
                )

                //Display a row of chips
                ExerciseCategoryChipRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    exercise = exercise
                )
            }
        }
    }
}

/**
 * Displays the icon of the exercise within an outlined circle
 */
@Composable
private fun ExerciseTypeIcon(modifier: Modifier, exercise: Exercise) {
    Box(modifier = modifier) {
        Icon(
            modifier = Modifier
                .fillMaxSize(),
            painter = getExerciseTypeIconPainter(exercise),
            contentDescription = null
        )
    }
}

/**
 * Displays a rectangle next to the text to show the difficulty of the exercise.
 */
@Composable
private fun DifficultyRectangle(modifier: Modifier, difficulty: Exercise.Difficulty) {
    Box(modifier = modifier
        .fillMaxHeight()
        .width(10.dp)
        .clip(RectangleShape)
        .background(
            color = when (difficulty) {
                Exercise.Difficulty.EASY ->
                    difficultyEasyColor

                Exercise.Difficulty.MEDIUM ->
                    difficultyMediumColor

                Exercise.Difficulty.HARD ->
                    difficultyHardColor
            }
        )
    )
}

/**
 * Displays the exercise due data and the participation info. The participation info is automatically updated.
 */
@Composable
private fun ExerciseDataText(
    modifier: Modifier,
    exercise: Exercise,
    displayActionButtons: Boolean,
    exerciseActions: ExerciseActions
) {
    // Format a relative time if the distant is
    val dueDate = exercise.dueDate
    val formattedDueDate = if (dueDate != null) {
        stringResource(
            id = R.string.exercise_item_due_date_set,
            getRelativeTime(to = dueDate)
        )
    } else stringResource(id = R.string.exercise_item_due_date_not_set)

    Column(modifier = modifier) {
        Text(
            text = formattedDueDate,
            style = MaterialTheme.typography.bodyMedium
        )

        ProvideDefaultExerciseTemplateStatus(exercise) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ParticipationStatusUi(
                    modifier = Modifier,
                    exercise = exercise
                )

                if (displayActionButtons) {
                    ExerciseActionButtons(
                        modifier = Modifier,
                        exercise = exercise,
                        showResult = true,
                        actions = exerciseActions
                    )
                }
            }
        }
    }
}
