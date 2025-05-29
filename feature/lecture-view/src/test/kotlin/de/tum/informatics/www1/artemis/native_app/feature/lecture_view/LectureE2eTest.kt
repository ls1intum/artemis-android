package de.tum.informatics.www1.artemis.native_app.feature.lecture_view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToKey
import androidx.lifecycle.SavedStateHandle
import androidx.test.platform.app.InstrumentationRegistry
import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.data.service.Api
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.JsonProvider
import de.tum.informatics.www1.artemis.native_app.core.data.test.awaitFirstSuccess
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Lecture
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnit
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitExercise
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import de.tum.informatics.www1.artemis.native_app.core.test.coreTestModules
import de.tum.informatics.www1.artemis.native_app.core.test.testWebsocketModule
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createAttachment
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createCourse
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createExercise
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createExerciseLectureUnit
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createLecture
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createLectureUnit
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createOnlineLectureUnit
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createTextExercise
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createTextLectureUnit
import de.tum.informatics.www1.artemis.native_app.feature.lectureview.LectureScreen
import de.tum.informatics.www1.artemis.native_app.feature.lectureview.LectureViewModel
import de.tum.informatics.www1.artemis.native_app.feature.lectureview.R
import de.tum.informatics.www1.artemis.native_app.feature.lectureview.TEST_TAG_OVERVIEW_LIST
import de.tum.informatics.www1.artemis.native_app.feature.lectureview.getLectureUnitTestTag
import de.tum.informatics.www1.artemis.native_app.feature.lectureview.lectureModule
import de.tum.informatics.www1.artemis.native_app.feature.lectureview.lecture_units.TEST_TAG_CHECKBOX_LECTURE_UNIT_COMPLETED
import de.tum.informatics.www1.artemis.native_app.feature.login.loginModule
import de.tum.informatics.www1.artemis.native_app.feature.login.test.getAdminAccessToken
import de.tum.informatics.www1.artemis.native_app.feature.login.test.performTestLogin
import de.tum.informatics.www1.artemis.native_app.feature.login.test.testLoginModule
import de.tum.informatics.www1.artemis.native_app.feature.metis.communicationModule
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.test.KoinTestRule
import org.koin.test.get
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
class LectureE2eTest : BaseComposeTest() {

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        androidContext(InstrumentationRegistry.getInstrumentation().context)

        modules(coreTestModules)
        modules(loginModule, lectureModule, testLoginModule, testWebsocketModule, communicationModule)
    }

    private lateinit var course: Course
    private lateinit var lecture: Lecture

    @Before
    fun setup() {
        runBlockingWithTestTimeout {
            performTestLogin()

            course = createCourse(getAdminAccessToken())
            lecture = createLecture(getAdminAccessToken(), course.id!!)
        }
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `shows correct title in overview`() {
        setupViewModelAndUi()

        composeTestRule.onNodeWithText(lecture.title).assertExists()
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `shows lecture description`() {
        val description = lecture.description
        assertNotNull(description)
        setupViewModelAndUi()

        composeTestRule.onNodeWithText(description).assertExists()
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `shows text lecture unit`() {
        verifyLectureUnit(createLectureUnit("text-units", ::createTextLectureUnit))
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `shows exercise lecture unit`() {
        val exercise = runBlocking {
            createExercise(
                getAdminAccessToken(),
                course.id!!,
                pathSegments = Api.Text.TextExercises.path,
                creator = ::createTextExercise
            )
        }

        createLectureUnit("exercise-units") {
            createExerciseLectureUnit(
                it,
                get<JsonProvider>().applicationJsonConfiguration.encodeToString(exercise)
            )
        }.let { verifyLectureUnit(it) }
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `shows online lecture unit`() {
        verifyLectureUnit(createLectureUnit("online-units", ::createOnlineLectureUnit))
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    @Ignore("Lecture attachment creation has been deprecated in https://github.com/ls1intum/Artemis/pull/10708")
    fun `shows attachments`() {
        val attachments = runBlocking {
            (0 until 3).map {
                createAttachment(getAdminAccessToken(), lecture.id!!)
            }
        }

        assert(attachments.size == 3) { "Expected 3 lecture units" }

        val viewModel = setupViewModelAndUi()
        val loadedAttachments = runBlockingWithTestTimeout {
            viewModel.lectureDataState.awaitFirstSuccess("Lecture Data State").attachments
        }

        assertEquals(loadedAttachments.size, 3, "Expected 3 attachments")

        composeTestRule
            .onNodeWithText(context.getString(R.string.lecture_view_tab_attachments))
            .performClick()

        attachments.forEach { attachment ->
            composeTestRule.onNodeWithText(attachment.name!!).assertExists()
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test(timeout = DefaultTestTimeoutMillis)
    fun `mark lecture unit as completed is successful`() {
        val lectureUnit = createLectureUnit("text-units", ::createTextLectureUnit)

        val viewModel = setupViewModelAndUi()

        composeTestRule.onNodeWithTag(TEST_TAG_OVERVIEW_LIST).performScrollToKey(lectureUnit.id)

        val checkboxMatcher =
            hasParent(hasTestTag(getLectureUnitTestTag(lectureUnitId = lectureUnit.id))) and hasTestTag(
                TEST_TAG_CHECKBOX_LECTURE_UNIT_COMPLETED
            )

        // trigger mark as successful
        composeTestRule.onNode(checkboxMatcher).performClick()

        // Wait until complete
        composeTestRule.waitUntilAtLeastOneExists(checkboxMatcher, DefaultTimeoutMillis)

        // Check now actually completed.
        assertTrue(
            viewModel.lectureUnits.value.first { it.id == lectureUnit.id }.completed,
            "Lecture unit is not marked as completed"
        )
    }

    private fun createLectureUnit(
        endpoint: String,
        creator: (String) -> String
    ): LectureUnit {
        return runBlocking {
            createLectureUnit(
                getAdminAccessToken(),
                lecture.id!!,
                endpoint,
                creator
            )
        }
    }

    private fun verifyLectureUnit(lectureUnit: LectureUnit) {
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
            channelService = get(),
            serverTimeService = get(),
            courseExerciseService = get(),
            coroutineContext = testDispatcher
        )

        composeTestRule.setContent {
            LectureScreen(
                modifier = Modifier.fillMaxSize(),
                courseId = course.id!!,
                viewModel = viewModel,
                onViewExercise = {}
            )
        }

        composeTestRule.waitUntilAtLeastOneExists(
            hasTestTag(TEST_TAG_OVERVIEW_LIST),
            DefaultTimeoutMillis
        )

        return viewModel
    }
}
