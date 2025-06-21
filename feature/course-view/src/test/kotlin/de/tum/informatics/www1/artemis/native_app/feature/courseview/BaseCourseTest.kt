package de.tum.informatics.www1.artemis.native_app.feature.courseview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.test.platform.app.InstrumentationRegistry
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import de.tum.informatics.www1.artemis.native_app.core.test.coreTestModules
import de.tum.informatics.www1.artemis.native_app.core.test.testWebsocketModule
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createCourse
import de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.CourseViewModel
import de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.course_overview.CourseUiScreen
import de.tum.informatics.www1.artemis.native_app.feature.login.loginModule
import de.tum.informatics.www1.artemis.native_app.feature.login.test.getAdminAccessToken
import de.tum.informatics.www1.artemis.native_app.feature.login.test.performTestLogin
import de.tum.informatics.www1.artemis.native_app.feature.login.test.testLoginModule
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter.LocalVisibleMetisContextManager
import de.tum.informatics.www1.artemis.native_app.feature.metistest.VisibleMetisContextManagerMock
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
            courseExerciseService = get(),
            networkStatusProvider = get(),
            coroutineContext = testDispatcher
        )

        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalVisibleMetisContextManager provides VisibleMetisContextManagerMock
            ) {
                CourseUiScreen(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    courseId = course.id!!,
                    onNavigateToExercise = {},
                    onNavigateToExerciseResultView = { _: Long -> },
                    onNavigateToTextExerciseParticipation = { _: Long, _: Long -> },
                    onParticipateInQuiz = { _: Long, _: Boolean -> },
                    onClickViewQuizResults = { _: Long, _: Long -> },
                    onNavigateToLecture = {},
                    onNavigateToFaq = {},
                    onNavigateBack = {},
                    onNavigateNotificationSection = { _: Long -> }
                )
            }
        }

        return viewModel
    }

    internal fun hasTestTagEndingWith(suffix: String): SemanticsMatcher {
        return SemanticsMatcher("has test tag ending with $suffix") { semanticsNode ->
            val tag = semanticsNode.config.getOrNull(SemanticsProperties.TestTag)
            tag?.endsWith(suffix) == true
        }
    }
}