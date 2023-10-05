package de.tum.informatics.www1.artemis.native_app.feature.courseview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import de.tum.informatics.www1.artemis.native_app.core.data.CourseServiceFake
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.CourseExerciseService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountServiceStub
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationServiceStub
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProviderStub
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.ModelingExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.ProgrammingExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.TextExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.ui.ScreenshotFrame
import de.tum.informatics.www1.artemis.native_app.core.websocket.LiveParticipationService
import de.tum.informatics.www1.artemis.native_app.core.websocket.test.LiveParticipationServiceStub
import de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.CourseViewModel
import de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.course_overview.CourseUiScreen
import de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.course_overview.DEFAULT_CONVERSATION_ID
import de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.course_overview.DEFAULT_POST_ID
import org.koin.core.context.startKoin
import org.koin.dsl.module

@Preview(widthDp = 500, heightDp = 900)
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
        courseService = CourseServiceFake(
            Course(
                title = "Advanced Aerospace Engineering \uD83D\uDE80",
                exercises = listOf(
                    ModelingExercise(
                        id = 1,
                        title = "Designing a rocket engine"
                    ),
                    TextExercise(
                        id = 2,
                        title = "Sending a rover to saturn \uD83E\uDE90"
                    ),
                    ProgrammingExercise(
                        id = 3,
                        title = "Heat control on atmospheric entry \uD83D\uDD25"
                    )
                )
            )
        ),
        liveParticipationService = LiveParticipationServiceStub(),
        serverConfigurationService = ServerConfigurationServiceStub(),
        accountService = AccountServiceStub(),
        courseExerciseService = object : CourseExerciseService {
            override suspend fun startExercise(
                exerciseId: Long,
                serverUrl: String,
                authToken: String
            ): NetworkResponse<Participation> = NetworkResponse.Failure(RuntimeException())
        },
        networkStatusProvider = NetworkStatusProviderStub()
    )

    ScreenshotFrame("Always have an overview of your exercises at hand") {
        CourseUiScreen(
            modifier = Modifier.fillMaxSize(),
            viewModel = courseViewModel,
            courseId = 0L,
            conversationId = DEFAULT_CONVERSATION_ID,
            postId = DEFAULT_POST_ID,
            onNavigateToExercise = {},
            onNavigateToTextExerciseParticipation = { _, _ -> },
            onNavigateToExerciseResultView = {},
            onParticipateInQuiz = { _, _ -> },
            onClickViewQuizResults = { _, _ -> },
            onNavigateToLecture = {},
            onNavigateBack = {}
        )
    }
}
