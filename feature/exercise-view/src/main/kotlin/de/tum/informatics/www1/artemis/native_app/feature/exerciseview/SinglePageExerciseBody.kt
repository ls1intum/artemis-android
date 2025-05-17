package de.tum.informatics.www1.artemis.native_app.feature.exerciseview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.ui.ArtemisAppLayout
import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.timeframe.TimeFrame
import de.tum.informatics.www1.artemis.native_app.core.ui.common.tablet.LayoutAwareTwoColumnLayout
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.CollapsingContentState
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.BoundExerciseActions
import de.tum.informatics.www1.artemis.native_app.core.ui.getArtemisAppLayout
import de.tum.informatics.www1.artemis.native_app.core.ui.isTabletPortrait
import androidx.activity.compose.BackHandler

@Composable
fun SinglePageExerciseBody(
    modifier: Modifier,
    exercises: List<TimeFrame<Exercise>>,
    query: String,
    collapsingContentState: CollapsingContentState,
    onClickExercise: (Long) -> Unit,
    actions: BoundExerciseActions,
    title: String
) {
    val layout = getArtemisAppLayout()
    val isTabletPortrait = layout.isTabletPortrait

    var config: ExerciseConfiguration by rememberSaveable { mutableStateOf(NothingOpened) }
    var isSidebarOpen by remember { mutableStateOf(true) }
    val onSidebarToggle: () -> Unit = { isSidebarOpen = !isSidebarOpen }

    val openExercise: (Long) -> Unit = { id ->
        if (isTabletPortrait) isSidebarOpen = false
        config = OpenedExercise(config, id)
    }

    val resetToSidebar = {
        isSidebarOpen = true
        config = NothingOpened
    }

    // Handle system back button
    BackHandler(enabled = config is OpenedExercise) {
        if (isTabletPortrait) {
            // In portrait mode, first back press resets to exercise list
            resetToSidebar()
        }
    }

    when (layout) {
        ArtemisAppLayout.Phone -> {
            ExerciseOverviewBody(
                modifier = modifier,
                exercises = exercises,
                query = query,
                collapsingContentState = collapsingContentState,
                onClickExercise = onClickExercise,
                selectedExerciseId = null // No selection in phone mode
            )
        }

        ArtemisAppLayout.Tablet -> {
            LayoutAwareTwoColumnLayout(
                modifier = modifier,
                isSidebarOpen = isSidebarOpen,
                onSidebarToggle = onSidebarToggle,
                optionalColumn = { sideMod ->
                    ExerciseOverviewBody(
                        modifier = sideMod,
                        exercises = exercises,
                        query = query,
                        collapsingContentState = collapsingContentState,
                        onClickExercise = openExercise,
                        selectedExerciseId = (config as? OpenedExercise)?.exerciseId
                    )
                },
                priorityColumn = { contentMod ->
                    when (val conf = config) {
                        NothingOpened -> {
                            IconButton(onClick = onSidebarToggle) {
                                Icon(
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = "Open sidebar"
                                )
                            }
                            Box(
                                modifier = contentMod.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(stringResource(id = R.string.exercise_list_exercise_item_not_selected))
                            }
                        }

                        is OpenedExercise -> {
                            ExerciseDetailContent(
                                exerciseId = conf.exerciseId,
                                viewMode = ExerciseViewMode.Overview,
                                onNavigateBack = {
                                    resetToSidebar()
                                },
                                actions = actions,
                                onSidebarToggle = onSidebarToggle
                            )
                        }
                    }
                },
                title = title
            )
        }
    }
}

@Composable
fun ExerciseOverviewBody(
    modifier: Modifier,
    exercises: List<TimeFrame<Exercise>>,
    query: String,
    collapsingContentState: CollapsingContentState,
    onClickExercise: (Long) -> Unit,
    selectedExerciseId: Long?
) {
    ExerciseListUi(
        modifier = modifier,
        exercises = exercises,
        query = query,
        collapsingContentState = collapsingContentState,
        onClickExercise = onClickExercise,
        selectedExerciseId = selectedExerciseId
    )
}
