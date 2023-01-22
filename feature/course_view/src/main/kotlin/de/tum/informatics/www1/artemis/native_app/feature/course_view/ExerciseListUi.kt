package de.tum.informatics.www1.artemis.native_app.feature.course_view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseActions
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseListItem

/**
 * Display a list of all exercises with section headers.
 * The exercises are clickable.
 */
@Composable
internal fun ExerciseListUi(
    modifier: Modifier,
    weeklyExercises: List<GroupedByWeek<Exercise>>,
    onClickExercise: (exerciseId: Long) -> Unit,
    onClickStartTextExercise: (exerciseId: Long) -> Unit,
    onClickPracticeQuiz: (exerciseId: Long) -> Unit,
    onClickOpenQuiz: (exerciseId: Long) -> Unit,
    onClickStartQuiz: (exerciseId: Long) -> Unit,
    onClickOpenTextExercise: (exerciseId: Long, participationId: Long) -> Unit,
    onClickViewResult: (exerciseId: Long) -> Unit
) {
    WeeklyItemsLazyColumn(
        modifier = modifier,
        weeklyItemGroups = weeklyExercises,
        getItemId = { id }
    ) { exercise ->
        val actions = remember(exercise.id) {
            ExerciseActions(
                onClickStartTextExercise = { onClickStartTextExercise(exercise.id) },
                onClickPracticeQuiz = { onClickPracticeQuiz(exercise.id) },
                onClickOpenQuiz = { onClickOpenQuiz(exercise.id) },
                onClickStartQuiz = { onClickStartQuiz(exercise.id) },
                onClickOpenTextExercise = { onClickOpenTextExercise(exercise.id, it) },
                onClickViewResult = { onClickViewResult(exercise.id) },
            )
        }

        ExerciseListItem(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            exercise = exercise,
            exerciseActions = actions,
            onClickExercise = { onClickExercise(exercise.id) }
        )
    }
}
