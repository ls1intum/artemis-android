import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import de.tum.informatics.www1.artemis.native_app.core.common.test.PlayStoreScreenshotTest
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.CourseExerciseService
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.CourseService
import de.tum.informatics.www1.artemis.native_app.core.data.test.service.CourseServiceFake
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.CourseWithScore
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.test.screenshots.BasePaparazziTest
import de.tum.informatics.www1.artemis.native_app.core.websocket.LiveParticipationService
import de.tum.informatics.www1.artemis.native_app.core.websocket.test.LiveParticipationServiceStub
import de.tum.informatics.www1.artemis.native_app.datastore.test.AccountServiceStub
import de.tum.informatics.www1.artemis.native_app.datastore.test.ServerConfigurationServiceStub
import de.tum.informatics.www1.artemis.native_app.device.test.NetworkStatusProviderStub
import de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.CourseViewModel
import de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.course_overview.CourseUiScreen
import org.junit.Test
import org.junit.experimental.categories.Category

@Category(PlayStoreScreenshotTest::class)
class CourseScreenScreenshots : BasePaparazziTest() {

    @Test
    fun `Course Screen - Exercise List`() {
        val courseViewModel = CourseViewModel(
            courseId = 0L,
            courseService = CourseServiceFake(Course(title = "Foo")),
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

        paparazzi.snapshot("CourseScreen_ExerciseList") {
            CourseUiScreen(
                modifier = Modifier.fillMaxSize(),
                viewModel = courseViewModel,
                courseId = 0L,
                conversationId = 0L,
                postId = 0L,
                onNavigateToExercise = {},
                onNavigateToTextExerciseParticipation = {_, _ -> },
                onNavigateToExerciseResultView = {},
                onParticipateInQuiz = { _, _ -> },
                onClickViewQuizResults = {_, _ -> },
                onNavigateToLecture = {},
                onNavigateBack = {}
            )
        }
    }
}