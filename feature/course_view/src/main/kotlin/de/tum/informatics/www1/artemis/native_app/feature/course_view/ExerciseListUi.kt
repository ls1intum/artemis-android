package de.tum.informatics.www1.artemis.native_app.feature.course_view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.BoundExerciseActions
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseListItem

/**
 * Display a list of all exercises with section headers.
 * The exercises are clickable.
 */
@Composable
internal fun ExerciseListUi(
    modifier: Modifier,
    weeklyExercises: List<GroupedByWeek<Exercise>>,
    actions: BoundExerciseActions,
    onClickExercise: (exerciseId: Long) -> Unit
) {
    WeeklyItemsLazyColumn(
        modifier = modifier,
        weeklyItemGroups = weeklyExercises,
        getItemId = { id }
    ) { exercise ->
        ExerciseListItem(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            exercise = exercise,
            exerciseActions = remember(exercise, actions) { actions.getUnbound(exerciseId = exercise.id) },
            onClickExercise = { onClickExercise(exercise.id) }
        )
    }
}
