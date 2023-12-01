package de.tum.informatics.www1.artemis.native_app.feature.dashboard

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountServiceStub
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationServiceStub
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProviderStub
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.CourseWithScore
import de.tum.informatics.www1.artemis.native_app.core.model.Dashboard
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.TextExercise
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Lecture
import de.tum.informatics.www1.artemis.native_app.core.ui.CourseImageProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.LocalCourseImageProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.PlayStoreScreenshots
import de.tum.informatics.www1.artemis.native_app.core.ui.ScreenshotFrame
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.service.DashboardService

private const val IMAGE_MARS = "mars"
private const val IMAGE_SATURN_5 = "saturn5"

@PlayStoreScreenshots
@Composable
fun `Dashboard - Exercise List`() {
    val viewModel = CourseOverviewViewModel(
        dashboardService = object : DashboardService {
            override suspend fun loadDashboard(
                authToken: String,
                serverUrl: String
            ): NetworkResponse<Dashboard> = NetworkResponse.Response(
                Dashboard(
                    courses = listOf(
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
        accountService = AccountServiceStub(),
        serverConfigurationService = ServerConfigurationServiceStub(),
        networkStatusProvider = NetworkStatusProviderStub()
    )

    val betaHintService = remember { BetaHintServiceFake() }

    val fakeCourseImageProvider = remember {
        object : CourseImageProvider {
            @Composable
            override fun rememberCourseImagePainter(
                courseIconPath: String,
                serverUrl: String,
                authorizationToken: String
            ): Painter {
                return painterResource(
                    id = when (courseIconPath) {
                        IMAGE_MARS -> R.drawable.mars
                        else -> R.drawable.saturn5
                    }
                )
            }

        }
    }

    CompositionLocalProvider(LocalCourseImageProvider provides fakeCourseImageProvider) {
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