package de.tum.informatics.www1.artemis.native_app.core.ui.exercise

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.ui.R
import de.tum.informatics.www1.artemis.native_app.core.ui.date.getRelativeTime

/**
 * Display a single exercise.
 * The exercise is displayed in a card with an icon specific to the exercise type.
 */
@Composable
fun ExerciseListItem(
    modifier: Modifier,
    exercise: Exercise,
    onClickExercise: () -> Unit
) {
    Card(modifier = modifier, onClick = onClickExercise) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                ExerciseTypeIcon(modifier = Modifier.size(80.dp), exercise = exercise)

                ExerciseDataText(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    exercise = exercise
                )
            }

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

/**
 * Displays the icon of the exercise within an outlined circle
 */
@Composable
private fun ExerciseTypeIcon(modifier: Modifier, exercise: Exercise) {
    Box(modifier = modifier) {
        Icon(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            painter = getExerciseTypeIconPainter(exercise),
            contentDescription = null
        )
    }
}

/**
 * Displays the exercise title, the due data and the participation info. The participation info is automatically updated.
 */
@Composable
private fun ExerciseDataText(modifier: Modifier, exercise: Exercise) {
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
            text = exercise.title.orEmpty(),
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = formattedDueDate,
            style = MaterialTheme.typography.bodyMedium
        )

        ParticipationStatusUi(
            modifier = Modifier.fillMaxWidth(),
            exercise = exercise,
        )
    }
}