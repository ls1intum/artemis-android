package de.tum.informatics.www1.artemis.native_app.feature.courseview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.test.platform.app.InstrumentationRegistry
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import de.tum.informatics.www1.artemis.native_app.core.test.coreTestModules
import de.tum.informatics.www1.artemis.native_app.core.test.testWebsocketModule
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createCourse
import de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.CourseViewModel
import de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.course_overview.CourseUiScreen
import de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.course_overview.DEFAULT_CONVERSATION_ID
import de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.course_overview.DEFAULT_POST_ID
import de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.course_overview.DEFAULT_USERNAME
import de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.course_overview.DEFAULT_USER_ID
import de.tum.informatics.www1.artemis.native_app.feature.login.loginModule
import de.tum.informatics.www1.artemis.native_app.feature.login.test.getAdminAccessToken
import de.tum.informatics.www1.artemis.native_app.feature.login.test.performTestLogin
import de.tum.informatics.www1.artemis.native_app.feature.login.test.testLoginModule
import org.junit.Before
import org.junit.Rule
import org.koin.android.ext.koin.androidContext
import org.koin.test.KoinTestRule
import org.koin.test.get

abstract class BaseCourseTest : BaseComposeTest() {

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        androidContext(InstrumentationRegistry.getInstrumentation().context)

        modules(coreTestModules)
        modules(loginModule, courseViewModule, testLoginModule, testWebsocketModule)
    }

    lateinit var course: Course

    @Before
    fun setup() {
        runBlockingWithTestTimeout {
            performTestLogin()
            course = createCourse(getAdminAccessToken())
        }
    }

    internal fun setupAndDisplayCourseUi(): CourseViewModel {
        val viewModel = CourseViewModel(
            courseId = course.id!!,
            courseService = get(),
            liveParticipationService = get(),
            serverConfigurationService = get(),
            accountService = get(),
            courseExerciseService = get(),
            networkStatusProvider = get(),
            coroutineContext = testDispatcher
        )

        composeTestRule.setContent {
            CourseUiScreen(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
                courseId = course.id!!,
                conversationId = DEFAULT_CONVERSATION_ID,
                postId = DEFAULT_POST_ID,
                username = DEFAULT_USERNAME,
                userId = DEFAULT_USER_ID,
                onNavigateToExercise = {},
                onNavigateToExerciseResultView = {},
                onNavigateToTextExerciseParticipation = { _, _ -> },
                onParticipateInQuiz = { _, _ -> },
                onClickViewQuizResults = { _, _ -> },
                onNavigateToLecture = {},
                onNavigateBack = {}
            )
        }

        return viewModel
    }
}