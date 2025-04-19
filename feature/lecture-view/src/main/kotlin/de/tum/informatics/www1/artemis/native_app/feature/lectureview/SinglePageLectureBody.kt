package de.tum.informatics.www1.artemis.native_app.feature.lectureview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Lecture
import de.tum.informatics.www1.artemis.native_app.core.ui.ArtemisAppLayout
import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.timeframe.TimeFrame
import de.tum.informatics.www1.artemis.native_app.core.ui.common.tablet.LayoutAwareTwoColumnLayout
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.CollapsingContentState
import de.tum.informatics.www1.artemis.native_app.core.ui.getArtemisAppLayout
import de.tum.informatics.www1.artemis.native_app.core.ui.isTabletPortrait

@Composable
fun SinglePageLectureBody(
    modifier: Modifier,
    lectures: List<TimeFrame<Lecture>>,
    query: String,
    collapsingContentState: CollapsingContentState,
    onViewExercise: (Long) -> Unit,
    onNavigateToExerciseResultView: (Long) -> Unit,
    onNavigateToTextExerciseParticipation: (Long, Long) -> Unit,
    onParticipateInQuiz: (Long, Boolean) -> Unit,
    onClickViewQuizResults: (Long, Long) -> Unit,
    onNavigateToLectureScreen: (Long) -> Unit,
) {

    val layout = getArtemisAppLayout()
    val isTabletPortrait = layout.isTabletPortrait

    var config: LectureConfiguration by rememberSaveable { mutableStateOf(NothingOpened) }
    var isSidebarOpen by rememberSaveable { mutableStateOf(true) }

    val openLecture: (Long) -> Unit = { id ->
        if (isTabletPortrait) isSidebarOpen = false
        config = OpenedLecture(_prev = config, lectureId = id)
    }

    when (layout) {
        ArtemisAppLayout.Phone -> {
            LectureOverviewBody(
                modifier = modifier,
                lectures = lectures,
                collapsingContentState = collapsingContentState,
                query = query,
                onSelectLecture = onNavigateToLectureScreen
            )
        }

        ArtemisAppLayout.Tablet -> {
            LayoutAwareTwoColumnLayout(
                modifier = modifier,
                isSidebarOpen = isSidebarOpen,
                onSidebarToggle = { isSidebarOpen = !isSidebarOpen },

                optionalColumn = { sideMod ->
                    LectureOverviewBody(
                        modifier = sideMod,
                        lectures = lectures,
                        collapsingContentState = collapsingContentState,
                        query = query,
                        onSelectLecture = openLecture
                    )
                },

                priorityColumn = { contentMod ->
                    when (val conf = config) {
                        NothingOpened -> Box(
                            modifier = contentMod.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) { Text(stringResource(R.string.lecture_list_lecture_item_not_selected)) }

                        is OpenedLecture -> LectureDetailContent(
                            lectureId = conf.lectureId,
                            onViewExercise = onViewExercise,
                            onNavigateToExerciseResultView = onNavigateToExerciseResultView,
                            onNavigateToTextExerciseParticipation = onNavigateToTextExerciseParticipation,
                            onParticipateInQuiz = onParticipateInQuiz,
                            onClickViewQuizResults = onClickViewQuizResults
                        )
                    }
                }
            )
        }
    }
}
