package de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.exercise_list

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.BoundExerciseActions
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseListItem
import de.tum.informatics.www1.artemis.native_app.feature.courseview.GroupedByWeek
import de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.WeeklyItemsLazyColumn

internal const val TEST_TAG_EXERCISE_LIST_LAZY_COLUMN = "exercise list lazy column"

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
        modifier = modifier.testTag(TEST_TAG_EXERCISE_LIST_LAZY_COLUMN),
        weeklyItemGroups = weeklyExercises,
        getItemId = { id ?: 0 }
    ) { exercise ->
        ExerciseListItem(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacings.ScreenHorizontalSpacing),
            exercise = exercise,
            exerciseActions = remember(exercise, actions) { actions.getUnbound(exerciseId = exercise.id ?: 0) },
            onClickExercise = { onClickExercise(exercise.id ?: 0) }
        )
    }
}
