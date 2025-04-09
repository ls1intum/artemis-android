package de.tum.informatics.www1.artemis.native_app.feature.dashboard

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.model.Dashboard
import de.tum.informatics.www1.artemis.native_app.core.ui.PlayStoreScreenshots
import de.tum.informatics.www1.artemis.native_app.core.ui.ScreenshotData
import de.tum.informatics.www1.artemis.native_app.core.ui.ScreenshotFrame
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.ui.CourseSorting
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.ui.CoursesOverview

@PlayStoreScreenshots
@Composable
fun `Dashboard - Exercise List`() {
    ScreenshotFrame(title = "Manage all of your courses in one app") {
        CoursesOverview(
            modifier = Modifier.fillMaxSize(),
            coursesDataState = DataState.Success(
                Dashboard(
                    courses = listOf(
                        ScreenshotData.course1WithScores,
                        ScreenshotData.course2WithScores,
                    ).toMutableList()
                )
            ),
            sorting = CourseSorting.ALPHABETICAL_ASCENDING,
            query = "",
            isBeta = false,
            onOpenSettings = { },
            onClickRegisterForCourse = { },
            onViewCourse = {},
            betaHintService = BetaHintServiceFake(),
            surveyHintService =  SurveyHintServiceFake(),
            onUpdateQuery = { },
            onUpdateSorting = { },
            onRequestReloadDashboard = { },
        )
    }
}