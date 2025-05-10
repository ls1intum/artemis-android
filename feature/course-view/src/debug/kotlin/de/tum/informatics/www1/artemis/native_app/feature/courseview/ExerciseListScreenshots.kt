package de.tum.informatics.www1.artemis.native_app.feature.courseview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.ui.PlayStoreScreenshots
import de.tum.informatics.www1.artemis.native_app.core.ui.ScreenshotData
import de.tum.informatics.www1.artemis.native_app.core.ui.ScreenshotFrame
import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.timeframe.TimeFrame
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.CollapsingContentState
import de.tum.informatics.www1.artemis.native_app.core.websocket.LiveParticipationService
import de.tum.informatics.www1.artemis.native_app.core.websocket.test.LiveParticipationServiceStub
import de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.course_overview.CourseScaffold
import de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.course_overview.CourseTab
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.ExerciseOverviewBody
import org.koin.core.context.startKoin
import org.koin.dsl.module

@PlayStoreScreenshots
@Composable
fun `Course View - Exercise List`() {
    startKoin {
        modules(
            module {
                single<LiveParticipationService> { LiveParticipationServiceStub() }
            }
        )
    }

    ScreenshotFrame("Always have an overview of your exercises at hand ...") {
        CourseScaffold(
            modifier = Modifier,
            courseDataState = DataState.Success(
                ScreenshotData.course1
            ),
            isCourseTabSelected = {
                it is CourseTab.Exercises
            },
            searchConfiguration = ScreenshotData.Util.searchConfiguration("Search for an exercise"),
            collapsingContentState = CollapsingContentState(),
            updateSelectedCourseTab = {},
            onNavigateBack = {},
            onReloadCourse = {}
        ) {
            ExerciseOverviewBody(
                modifier = Modifier
                    .fillMaxSize(),
                exercises = listOf(TimeFrame.Current(ScreenshotData.exercises)),
                query = "",
                collapsingContentState = CollapsingContentState(),
                onClickExercise = { },
                selectedExerciseId = null
            )
        }
    }
}
