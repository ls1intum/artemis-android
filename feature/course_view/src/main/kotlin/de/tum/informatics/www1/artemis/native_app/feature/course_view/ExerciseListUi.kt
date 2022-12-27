package de.tum.informatics.www1.artemis.native_app.feature.course_view

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.*
import de.tum.informatics.www1.artemis.native_app.core.ui.date.getRelativeTime
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseCategoryChipRow
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ParticipationStatusUi
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.getExerciseTypeIcon

/**
 * Display a list of all exercises with section headers.
 * The exercises are clickable.
 */
@Composable
internal fun ExerciseListUi(
    modifier: Modifier,
    weeklyExercises: List<GroupedByWeek<Exercise>>,
    onClickExercise: (exerciseId: Long) -> Unit
) {
    WeeklyItemsLazyColumn(
        modifier = modifier,
        weeklyItemGroups = weeklyExercises,
        getItemId = { id ?: hashCode().toLong() }
    ) { exercise ->
        ExerciseItem(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            exercise = exercise
        ) { onClickExercise(exercise.id ?: return@ExerciseItem) }
    }
}

/**
 * Display a single exercise.
 * The exercise is displayed in a card with an icon specific to the exercise type.
 */
@Composable
private fun ExerciseItem(
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
    Box(
        modifier = modifier.then(
            Modifier
                .border(width = 1.dp, color = LocalContentColor.current, shape = CircleShape)
        )
    ) {
        Icon(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            imageVector = getExerciseTypeIcon(exercise),
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
            id = R.string.course_ui_exercise_item_due_date_set,
            getRelativeTime(to = dueDate)
        )
    } else stringResource(id = R.string.course_ui_exercise_item_due_date_not_set)

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