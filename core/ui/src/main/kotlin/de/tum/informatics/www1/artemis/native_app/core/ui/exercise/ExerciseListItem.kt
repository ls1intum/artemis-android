package de.tum.informatics.www1.artemis.native_app.core.ui.exercise

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.ui.R
import de.tum.informatics.www1.artemis.native_app.core.ui.common.selectionBorder
import de.tum.informatics.www1.artemis.native_app.core.ui.date.getRelativeTime
import de.tum.informatics.www1.artemis.native_app.core.ui.material.colors.ExerciseColors

/**
 * Display a single exercise.
 * The exercise is displayed in a card with an icon specific to the exercise type.
 */
@Composable
fun ExerciseListItem(
    modifier: Modifier,
    exercise: Exercise,
    selected: Boolean = false,
    onClickExercise: () -> Unit
) {
    Card(
        modifier = modifier.selectionBorder(selected),
        onClick = onClickExercise
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            exercise.difficulty?.let { DifficultyRectangle(modifier = Modifier, difficulty = it) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    //Displays the icon of the exercise
                    val painter = getExerciseTypeIconPainter(exercise)
                    painter?.let {
                        Icon(
                            modifier = Modifier
                                .size(40.dp)
                                .padding(end = 16.dp)
                                .fillMaxSize(),
                            painter = it,
                            contentDescription = null
                        )
                    }

                    //Displays the title of the exercise
                    Text(
                        text = exercise.title.orEmpty(),
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                ExerciseDataText(
                    modifier = Modifier,
                    exercise = exercise
                )

                //Display a row of chips
                ExerciseCategoryChipRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    exercise = exercise
                )
            }
        }
    }
}

/**
 * Displays a rectangle next to the text to show the difficulty of the exercise.
 */
@Composable
private fun DifficultyRectangle(modifier: Modifier, difficulty: Exercise.Difficulty) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(10.dp)
            .background(
                color = when (difficulty) {
                    Exercise.Difficulty.EASY ->
                        ExerciseColors.Difficulty.easy

                    Exercise.Difficulty.MEDIUM ->
                        ExerciseColors.Difficulty.medium

                    Exercise.Difficulty.HARD ->
                        ExerciseColors.Difficulty.hard
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
    exercise: Exercise
) {
    // Format a relative time if the distant is
    val dueDate = exercise.dueDate
    val formattedDueDate = if (dueDate != null) {
        getRelativeTime(to = dueDate).toString()
    } else stringResource(id = R.string.exercise_item_due_date_not_set)

    ProvideDefaultExerciseTemplateStatus(exercise) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = modifier) {
                Text(
                    text = formattedDueDate,
                    style = MaterialTheme.typography.bodyMedium
                )

                ParticipationStatusUi(
                    modifier = Modifier,
                    exercise = exercise,
                    showLargeIcon = true
                )
            }
        }
    }
}
