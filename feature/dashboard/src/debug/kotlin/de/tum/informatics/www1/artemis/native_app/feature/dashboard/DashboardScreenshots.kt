package de.tum.informatics.www1.artemis.native_app.feature.dashboard

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.LocalAsyncImagePreviewHandler
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.model.Dashboard
import de.tum.informatics.www1.artemis.native_app.core.ui.PlayStoreScreenshots
import de.tum.informatics.www1.artemis.native_app.core.ui.ScreenshotData
import de.tum.informatics.www1.artemis.native_app.core.ui.ScreenshotFrame
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.LocalArtemisImageProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.test.ArtemisImageProviderStub
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.ui.CourseSorting
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.ui.CoursesOverview

@OptIn(ExperimentalCoilApi::class)
@PlayStoreScreenshots
@Composable
fun `Dashboard - Exercise List`() {
    CompositionLocalProvider(
        LocalArtemisImageProvider provides ArtemisImageProviderStub(),
        LocalAsyncImagePreviewHandler provides ScreenshotData.Util.configImagePreviewHandler(),
    ) {
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
}