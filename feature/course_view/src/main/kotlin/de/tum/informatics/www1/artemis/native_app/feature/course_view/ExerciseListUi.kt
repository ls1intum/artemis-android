package de.tum.informatics.www1.artemis.native_app.feature.course_view

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.*
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseListItem

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
        ExerciseListItem(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            exercise = exercise
        ) { onClickExercise(exercise.id ?: return@ExerciseListItem) }
    }
}
