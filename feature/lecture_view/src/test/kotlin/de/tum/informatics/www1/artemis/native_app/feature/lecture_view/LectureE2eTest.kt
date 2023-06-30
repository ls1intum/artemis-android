package de.tum.informatics.www1.artemis.native_app.feature.lecture_view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToKey
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.compose.rememberNavController
import androidx.test.platform.app.InstrumentationRegistry
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.JsonProvider
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Lecture
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitExercise
import de.tum.informatics.www1.artemis.native_app.core.test.coreTestModules
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createCourse
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createExercise
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createExerciseLectureUnit
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createLecture
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createLectureUnit
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createOnlineLectureUnit
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createTextExercise
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createTextLectureUnit
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createVideoLectureUnit
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.setTestServerUrl
import de.tum.informatics.www1.artemis.native_app.feature.login.loginModule
import de.tum.informatics.www1.artemis.native_app.feature.login.test.getAdminAccessToken
import de.tum.informatics.www1.artemis.native_app.feature.login.test.performTestLogin
import de.tum.informatics.www1.artemis.native_app.feature.login.test.testLoginModule
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.serialization.encodeToString
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.get
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertNotNull

@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
class LectureE2eTest : KoinTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        androidContext(InstrumentationRegistry.getInstrumentation().context)

        modules(coreTestModules)
        modules(loginModule, lectureModule, testLoginModule)
    }

    private lateinit var course: Course
    private lateinit var lecture: Lecture

    @Before
    fun setup() {
        runBlocking {
            setTestServerUrl()
            performTestLogin()

            course = createCourse(getAdminAccessToken())
            lecture = createLecture(getAdminAccessToken(), course.id!!)
        }
    }

    @Test
    fun `shows correct title in overview`() {
        setupViewModelAndUi()

        composeTestRule.onNodeWithText(lecture.title).assertExists()
    }

    @Test
    fun `shows lecture description`() {
        val description = lecture.description
        assertNotNull(description)
        setupViewModelAndUi()

        composeTestRule.onNodeWithText(description).assertExists()
    }

    @Test
    fun `shows text lecture unit`() {
        createAndVerifyLectureUnit("text-units", ::createTextLectureUnit)
    }

    @Test
    fun `shows exercise lecture unit`() {
        val exercise = runBlocking {
            createExercise(
                getAdminAccessToken(),
                course.id!!,
                endpoint = "text-exercises",
                creator = ::createTextExercise
            )
        }

        createAndVerifyLectureUnit("exercise-units") {
            createExerciseLectureUnit(
                it,
                get<JsonProvider>().applicationJsonConfiguration.encodeToString(exercise)
            )
        }
    }

    @Test
    fun `shows video lecture unit`() {
        createAndVerifyLectureUnit("video-units", ::createVideoLectureUnit)
    }

    @Test
    fun `shows online lecture unit`() {
        createAndVerifyLectureUnit("online-units", ::createOnlineLectureUnit)
    }

    private fun createAndVerifyLectureUnit(endpoint: String, creator: (String) -> String) {
        val lectureUnit = runBlocking {
            createLectureUnit(
                getAdminAccessToken(),
                lecture.id!!,
                endpoint,
                creator
            )
        }

        setupViewModelAndUi()

        composeTestRule.onNodeWithTag(TEST_TAG_OVERVIEW_LIST).performScrollToKey(lectureUnit.id)

        if (lectureUnit is LectureUnitExercise) {
            assertNotNull(lectureUnit.exercise) { "Exercise is not set on exercise lecture unit" }
            assertNotNull(lectureUnit.exercise!!.title) { "Title is not set on exercise of exercise lecture unit" }

            composeTestRule.onNodeWithText(lectureUnit.exercise!!.title!!).assertExists()
        } else {
            composeTestRule.onNodeWithText(lectureUnit.name!!).assertExists()
        }
    }

    private fun setupViewModelAndUi(): LectureViewModel {
        val viewModel = LectureViewModel(
            lectureId = lecture.id!!,
            networkStatusProvider = get(),
            lectureService = get(),
            serverConfigurationService = get(),
            accountService = get(),
            liveParticipationService = get(),
            savedStateHandle = SavedStateHandle(),
            serverTimeService = get(),
            courseExerciseService = get(),
            coroutineContext = UnconfinedTestDispatcher()
        )

        composeTestRule.setContent {
            LectureScreen(
                modifier = Modifier.fillMaxSize(),
                courseId = course.id!!,
                lectureId = lecture.id!!,
                viewModel = viewModel,
                navController = rememberNavController(),
                onNavigateBack = { },
                onViewExercise = {},
                onNavigateToExerciseResultView = {},
                onNavigateToTextExerciseParticipation = { _, _ -> },
                onParticipateInQuiz = { _, _ -> },
                onClickViewQuizResults = { _, _ -> }
            )
        }

        return viewModel
    }
}
