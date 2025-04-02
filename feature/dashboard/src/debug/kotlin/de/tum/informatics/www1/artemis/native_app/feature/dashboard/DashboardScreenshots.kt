package de.tum.informatics.www1.artemis.native_app.feature.dashboard

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.res.imageResource
import coil3.annotation.ExperimentalCoilApi
import coil3.asImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import de.tum.informatics.www1.artemis.native_app.core.data.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationServiceStub
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProviderStub
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.CourseWithScore
import de.tum.informatics.www1.artemis.native_app.core.model.Dashboard
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.TextExercise
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Lecture
import de.tum.informatics.www1.artemis.native_app.core.ui.PlayStoreScreenshots
import de.tum.informatics.www1.artemis.native_app.core.ui.ScreenshotFrame
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.LocalArtemisImageProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.test.ArtemisImageProviderStub
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.service.DashboardService
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.service.DashboardStorageService
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.ui.CourseOverviewViewModel
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.ui.CoursesOverview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.datetime.Clock

private const val IMAGE_MARS = "mars"
private const val IMAGE_SATURN_5 = "saturn5"

@OptIn(ExperimentalCoilApi::class)
@PlayStoreScreenshots
@Composable
fun `Dashboard - Exercise List`() {
    val viewModel = CourseOverviewViewModel(
        dashboardService = object : DashboardService {
            override val onArtemisContextChanged = emptyFlow<ArtemisContext.LoggedIn>()
            override suspend fun loadDashboard(): NetworkResponse<Dashboard> = NetworkResponse.Response(
                Dashboard(
                    courses = mutableListOf(
                        CourseWithScore(
                            course = Course(
                                id = 1,
                                title = "Advanced Aerospace Engineering \uD83D\uDE80",
                                courseIconPath = IMAGE_SATURN_5,
                                exercises = (0 until 5).map { TextExercise() },
                                lectures = (0 until 3).map { Lecture() }
                            ),
                            totalScores = CourseWithScore.TotalScores(
                                100f,
                                100f,
                                studentScores = CourseWithScore.TotalScores.StudentScores(
                                    80f,
                                    0.8f,
                                    0.8f,
                                    0f
                                )
                            )
                        ),
                        CourseWithScore(
                            course = Course(
                                id = 2,
                                title = "Manned space travel \uD83D\uDC68\u200D\uD83D\uDE80 \uD83D\uDC69\u200D\uD83D\uDE80",
                                courseIconPath = IMAGE_MARS,
                                exercises = (0 until 8).map { TextExercise() },
                                lectures = (0 until 2).map { Lecture() }
                            ),
                            totalScores = CourseWithScore.TotalScores(
                                100f,
                                100f,
                                studentScores = CourseWithScore.TotalScores.StudentScores(
                                    90f,
                                    0.9f,
                                    0.9f,
                                    0f
                                )
                            )
                        )
                    )
                )
            )
        },
        dashboardStorageService = object : DashboardStorageService{
            override suspend fun onCourseAccessed(serverHost: String, courseId: Long) {}

            override suspend fun getLastAccesssedCourses(serverHost: String): Flow<Map<Long, Long>> {
                // return one course to show in the screenshot
                return object : Flow<Map<Long, Long>> {
                    override suspend fun collect(collector: FlowCollector<Map<Long, Long>>) {
                        collector.emit(mapOf(2L to Clock.System.now().toEpochMilliseconds()))
                    }
                }
            }
        },
        serverConfigurationService = ServerConfigurationServiceStub(),
        networkStatusProvider = NetworkStatusProviderStub()
    )

    val betaHintService = remember { BetaHintServiceFake() }

    val marsImage = ImageBitmap.imageResource(R.drawable.mars).asAndroidBitmap().asImage()
    val saturnImage = ImageBitmap.imageResource(R.drawable.saturn5).asAndroidBitmap().asImage()
    val previewHandler = AsyncImagePreviewHandler { request ->
        when (request.data) {
            IMAGE_MARS -> marsImage
            IMAGE_SATURN_5 -> saturnImage
            else -> null
        }
    }

    CompositionLocalProvider(
        LocalArtemisImageProvider provides ArtemisImageProviderStub(),
        LocalAsyncImagePreviewHandler provides previewHandler
    ) {
        ScreenshotFrame(title = "Manage all of your courses in one app") {
            CoursesOverview(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
                isBeta = false,
                onOpenSettings = { },
                onClickRegisterForCourse = { },
                onViewCourse = {},
                betaHintService = betaHintService
            )
        }
    }
}