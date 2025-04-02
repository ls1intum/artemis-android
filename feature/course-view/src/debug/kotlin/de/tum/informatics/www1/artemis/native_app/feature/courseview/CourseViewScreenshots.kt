package de.tum.informatics.www1.artemis.native_app.feature.courseview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import de.tum.informatics.www1.artemis.native_app.core.data.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.data.CourseServiceFake
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.CourseExerciseService
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProviderStub
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.ui.PlayStoreScreenshots
import de.tum.informatics.www1.artemis.native_app.core.ui.ScreenshotFrame
import de.tum.informatics.www1.artemis.native_app.core.websocket.LiveParticipationService
import de.tum.informatics.www1.artemis.native_app.core.websocket.test.LiveParticipationServiceStub
import de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.CourseViewModel
import de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.course_overview.CourseUiScreen
import kotlinx.coroutines.flow.emptyFlow
import org.koin.core.context.startKoin
import org.koin.dsl.module

@PlayStoreScreenshots
@Preview
@Composable
fun `Course View - Exercise List`() {
    startKoin {
        modules(
            module {
                single<LiveParticipationService> { LiveParticipationServiceStub() }
            }
        )
    }

    val courseViewModel = CourseViewModel(
        courseId = 0L,
        courseService = CourseServiceFake(ScreenshotCourse),
        liveParticipationService = LiveParticipationServiceStub(),
        courseExerciseService = object : CourseExerciseService {
            override val onArtemisContextChanged = emptyFlow<ArtemisContext.LoggedIn>()
            override suspend fun startExercise(exerciseId: Long): NetworkResponse<Participation> =
                NetworkResponse.Failure(RuntimeException())
        },
        networkStatusProvider = NetworkStatusProviderStub()
    )

    ScreenshotFrame("Always have an overview of your exercises at hand") {
        CourseUiScreen(
            modifier = Modifier.fillMaxSize(),
            viewModel = courseViewModel,
            courseId = 0L,
            onNavigateToExercise = {},
            onNavigateToTextExerciseParticipation = { _, _ -> },
            onNavigateToExerciseResultView = {},
            onParticipateInQuiz = { _, _ -> },
            onClickViewQuizResults = { _, _ -> },
            onNavigateToLecture = {},
            onNavigateToFaq = {},
            onNavigateBack = {}
        )
    }
}
