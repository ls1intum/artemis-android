package de.tum.informatics.www1.artemis.native_app.feature.exerciseview

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.common.EmptyListHint
import de.tum.informatics.www1.artemis.native_app.core.ui.common.NoSearchResults
import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.timeframe.TimeFrame
import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.timeframe.TimeFrameItemsLazyColumn
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.CollapsingContentState
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseListItem

const val TEST_TAG_EXERCISE_LIST_LAZY_COLUMN = "exercise list lazy column"

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
    onClickExercise: (exerciseId: Long) -> Unit,
    selectedExerciseId: Long?
) {
    if (exercises.isEmpty()) {
        if (query.isNotBlank()) {
            NoSearchResults(
                modifier = modifier,
                title = stringResource(id = R.string.exercise_list_exercises_no_search_results_title),
                details = stringResource(id = R.string.exercise_list_exercises_no_search_results_body, query)
            )
            return
        }

        EmptyListHint(
            modifier = modifier,
            hint = stringResource(id = R.string.exercise_list_exercises_no_search_results_title),
            imageVector = Icons.Default.ListAlt
        )
        return
    }

    TimeFrameItemsLazyColumn(
        modifier = modifier
            .nestedScroll(collapsingContentState.nestedScrollConnection)
            .testTag(TEST_TAG_EXERCISE_LIST_LAZY_COLUMN),
        timeFrameGroup = exercises,
        query = query,
        getItemId = { id ?: 0 }
    ) { m, exercise ->
        ExerciseListItem(
            modifier = m
                .fillMaxWidth()
                .padding(horizontal = Spacings.ScreenHorizontalSpacing),
            exercise = exercise,
            selected = exercise.id == selectedExerciseId,
            onClickExercise = { onClickExercise(exercise.id ?: 0) }
        )
    }
}
