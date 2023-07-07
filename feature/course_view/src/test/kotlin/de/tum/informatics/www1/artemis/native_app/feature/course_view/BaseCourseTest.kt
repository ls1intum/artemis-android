package de.tum.informatics.www1.artemis.native_app.feature.course_view

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.test.coreTestModules
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createCourse
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.setTestServerUrl
import de.tum.informatics.www1.artemis.native_app.feature.course_view.ui.CourseViewModel
import de.tum.informatics.www1.artemis.native_app.feature.course_view.ui.course_overview.CourseUiScreen
import de.tum.informatics.www1.artemis.native_app.feature.course_view.ui.course_overview.DEFAULT_CONVERSATION_ID
import de.tum.informatics.www1.artemis.native_app.feature.course_view.ui.course_overview.DEFAULT_POST_ID
import de.tum.informatics.www1.artemis.native_app.feature.login.loginModule
import de.tum.informatics.www1.artemis.native_app.feature.login.test.getAdminAccessToken
import de.tum.informatics.www1.artemis.native_app.feature.login.test.performTestLogin
import de.tum.informatics.www1.artemis.native_app.feature.login.test.testLoginModule
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.withTimeout
import org.junit.Before
import org.junit.Rule
import org.koin.android.ext.koin.androidContext
import org.koin.compose.LocalKoinApplication
import org.koin.compose.LocalKoinScope
import org.koin.core.annotation.KoinInternalApi
import org.koin.mp.KoinPlatformTools
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.get

abstract class BaseCourseTest : KoinTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        androidContext(InstrumentationRegistry.getInstrumentation().context)

        modules(coreTestModules)
        modules(loginModule, courseViewModule, testLoginModule)
    }

    lateinit var course: Course

    val context: Context get() = InstrumentationRegistry.getInstrumentation().context

    @Before
    fun setup() {
        runBlocking {
            withTimeout(DefaultTimeoutMillis) {
                setTestServerUrl()
                performTestLogin()

                course = createCourse(getAdminAccessToken())
            }
        }
    }

    @OptIn(KoinInternalApi::class)
    internal fun setupAndDisplayCourseUi(): CourseViewModel {
        val viewModel = CourseViewModel(
            courseId = course.id!!,
            courseService = get(),
            liveParticipationService = get(),
            serverConfigurationService = get(),
            accountService = get(),
            courseExerciseService = get(),
            networkStatusProvider = get(),
            coroutineContext = UnconfinedTestDispatcher()
        )

        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext().get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                CourseUiScreen(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    courseId = course.id!!,
                    conversationId = DEFAULT_CONVERSATION_ID,
                    postId = DEFAULT_POST_ID,
                    onNavigateToExercise = {},
                    onNavigateToExerciseResultView = {},
                    onNavigateToTextExerciseParticipation = { _, _ -> },
                    onParticipateInQuiz = { _, _ -> },
                    onClickViewQuizResults = { _, _ -> },
                    onNavigateToLecture = {},
                    onNavigateBack = {}
                )
            }
        }

        return viewModel
    }
}