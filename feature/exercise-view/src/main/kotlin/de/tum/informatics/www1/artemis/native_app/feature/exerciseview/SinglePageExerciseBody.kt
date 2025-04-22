package de.tum.informatics.www1.artemis.native_app.feature.exerciseview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

@Composable
fun SinglePageExerciseBody(
    modifier: Modifier,
    exercises: List<TimeFrame<Exercise>>,
    query: String,
    collapsingContentState: CollapsingContentState,
    onClickExercise: (Long) -> Unit,
    actions: BoundExerciseActions
) {
    val layout = getArtemisAppLayout()
    val isTabletPortrait = layout.isTabletPortrait

    var config: ExerciseConfiguration by remember { mutableStateOf(NothingOpened) }
    var isSidebarOpen by remember { mutableStateOf(true) }

    val openExercise: (Long) -> Unit = { id ->
        if (isTabletPortrait) isSidebarOpen = false
        config = OpenedExercise(config, id)
    }

    when (layout) {
        ArtemisAppLayout.Phone -> {
            ExerciseOverviewBody(
                modifier = modifier,
                exercises = exercises,
                query = query,
                collapsingContentState = collapsingContentState,
                onClickExercise = onClickExercise
            )
        }

        ArtemisAppLayout.Tablet -> {
            LayoutAwareTwoColumnLayout(
                modifier = modifier,
                isSidebarOpen = isSidebarOpen,
                onSidebarToggle = { isSidebarOpen = !isSidebarOpen },
                optionalColumn = { sideMod ->
                    ExerciseOverviewBody(
                        modifier = sideMod,
                        exercises = exercises,
                        query = query,
                        collapsingContentState = collapsingContentState,
                        onClickExercise = openExercise
                    )
                },
                priorityColumn = { contentMod ->
                    when (val conf = config) {
                        NothingOpened -> {
                            Box(
                                modifier = contentMod.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                IconButton(onClick = { isSidebarOpen = true }) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.MenuOpen,
                                        contentDescription = null
                                    )
                                }
                                Text(stringResource(id = R.string.exercise_list_exercise_item_not_selected))
                            }
                        }

                        is OpenedExercise -> {
                            ExerciseDetailContent(
                                exerciseId = conf.exerciseId,
                                viewMode = ExerciseViewMode.Overview,
                                onNavigateBack = {
                                    isSidebarOpen = true
                                    config = NothingOpened
                                },
                                actions = actions,
                                onSidebarToggle = { isSidebarOpen = !isSidebarOpen }
                            )
                        }
                    }
                }
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
    onClickExercise: (Long) -> Unit
) {
    ExerciseListUi(
        modifier = modifier,
        exercises = exercises,
        query = query,
        collapsingContentState = collapsingContentState,
        onClickExercise = onClickExercise,
    )
}
