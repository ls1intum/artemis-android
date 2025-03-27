package de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.exercise_list

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.common.EmptyListHint
import de.tum.informatics.www1.artemis.native_app.core.ui.common.NoSearchResults
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.CollapsingContentState
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.BoundExerciseActions
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseListItem
import de.tum.informatics.www1.artemis.native_app.feature.courseview.TimeFrame
import de.tum.informatics.www1.artemis.native_app.feature.courseview.R
import de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.TimeFrameItemsLazyColumn

internal const val TEST_TAG_EXERCISE_LIST_LAZY_COLUMN = "exercise list lazy column"

/**
 * Display a list of all exercises with section headers.
 * The exercises are clickable.
 */
@Composable
internal fun ExerciseListUi(
    modifier: Modifier,
    exercises: List<TimeFrame<Exercise>>,
    query: String,
    collapsingContentState: CollapsingContentState,
    actions: BoundExerciseActions,
    onClickExercise: (exerciseId: Long) -> Unit
) {
    val nonEmptyTimeFrames = remember(exercises) {
        exercises.filter { it.items.isNotEmpty() }
    }

    val forceExpandKeys = remember(query, nonEmptyTimeFrames) {
        if (query.isNotBlank()) {
            nonEmptyTimeFrames.map { it.key }.toSet()
        } else emptySet()
    }

    if (exercises.isEmpty()) {
        if (query.isNotBlank()) {
            NoSearchResults(
                modifier = modifier,
                title = stringResource(id = R.string.course_ui_exercises_no_search_results_title),
                details = stringResource(id = R.string.course_ui_exercises_no_search_results_body, query)
            )
            return
        }

        EmptyListHint(
            modifier = modifier,
            hint = stringResource(id = R.string.course_ui_exercises_no_search_results_title),
            imageVector = Icons.Default.ListAlt
        )
        return
    }



    TimeFrameItemsLazyColumn(
        modifier = modifier
            .nestedScroll(collapsingContentState.nestedScrollConnection)
            .testTag(TEST_TAG_EXERCISE_LIST_LAZY_COLUMN),
        timeFrameGroup = exercises,
        forceExpand = forceExpandKeys,
        getItemId = { id ?: 0 }
    ) { m, exercise ->
        ExerciseListItem(
            modifier = m
                .fillMaxWidth()
                .padding(horizontal = Spacings.ScreenHorizontalSpacing),
            exercise = exercise,
            exerciseActions = remember(
                exercise,
                actions
            ) { actions.getUnbound(exerciseId = exercise.id ?: 0) },
            onClickExercise = { onClickExercise(exercise.id ?: 0) }
        )
    }
}
