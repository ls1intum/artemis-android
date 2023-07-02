package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.participate.text_exercise

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.data.service.CourseExerciseService
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.TextExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.StudentParticipation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.SubmissionType
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.TextSubmission
import de.tum.informatics.www1.artemis.native_app.core.test.coreTestModules
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createCourse
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createExercise
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createTextExercise
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.setTestServerUrl
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.testServerUrl
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.ExerciseViewModel
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.exerciseModule
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.service.TextSubmissionService
import de.tum.informatics.www1.artemis.native_app.feature.login.loginModule
import de.tum.informatics.www1.artemis.native_app.feature.login.test.getAdminAccessToken
import de.tum.informatics.www1.artemis.native_app.feature.login.test.performTestLogin
import de.tum.informatics.www1.artemis.native_app.feature.login.test.testLoginModule
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
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
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.R
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.seconds

@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
class TextExerciseParticipationE2eTest : KoinTest {

    private val context: Context get() = InstrumentationRegistry.getInstrumentation().context

    @get:Rule
    val composeTestRole = createComposeRule()

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        androidContext(InstrumentationRegistry.getInstrumentation().context)

        modules(coreTestModules)
        modules(loginModule, exerciseModule, testLoginModule)
    }

    private lateinit var accessToken: String

    private lateinit var course: Course
    private lateinit var exercise: TextExercise
    private lateinit var participation: Participation

    private lateinit var initialSubmission: TextSubmission

    private val textToEnter =
        "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam"

    @Before
    fun setup() {
        runBlocking {
            setTestServerUrl()
            accessToken = performTestLogin()

            course = createCourse(getAdminAccessToken())
            exercise = createExercise(
                getAdminAccessToken(),
                course.id!!,
                endpoint = "text-exercises",
                creator = ::createTextExercise
            ) as TextExercise

            val courseExerciseService: CourseExerciseService = get()
            participation =
                courseExerciseService
                    .startExercise(exercise.id!!, testServerUrl, accessToken)
                    .orThrow("Start text exercise participation")

            val submissions = participation.submissions
            assertNotNull(submissions, "Submissions are not given in participation")
            assertFalse(submissions.isEmpty(), "Submissions must not be empty")

            initialSubmission = assertIs(submissions.first())
        }
    }

    @Test
    fun `can view already entered text`() {
        val textSubmissionService: TextSubmissionService = get()

        runBlocking {
            textSubmissionService.update(
                TextSubmission(
                    id = initialSubmission.id,
                    submissionDate = Clock.System.now(),
                    participation = StudentParticipation.StudentParticipationImpl(id = participation.id!!),
                    submitted = true,
                    text = textToEnter,
                    submissionType = SubmissionType.MANUAL
                ),
                exercise.id!!,
                testServerUrl,
                accessToken
            ).orThrow("Could no update text submission to set initial submission")
        }

        setupUi()

        composeTestRole
            .onNodeWithTag(TEST_TAG_TEXT_FIELD_PARTICIPATION)
            .assert(hasText(textToEnter))
    }

    @Test
    fun `can update text by entering new text`() {
        val testDispatcher = UnconfinedTestDispatcher()
        val viewModel = setupUi(testDispatcher)

        composeTestRole
            .onNodeWithTag(TEST_TAG_TEXT_FIELD_PARTICIPATION)
            .performTextInput(textToEnter)

        composeTestRole
            .onNodeWithText(context.getString(R.string.participate_text_exercise_submit_button))
            .performClick()

        // Advance time to continue with the syncing
        testDispatcher.scheduler.advanceTimeBy(TextExerciseParticipationViewModel.SyncDelay + 1.seconds)

        runBlocking {
            withTimeoutOrNull(1000) {
                viewModel.syncState.filterIsInstance<SyncState.Synced>().first()
            } ?: throw RuntimeException("State could not be synced in time.")
        }

        composeTestRole
            .onNodeWithText(context.getString(R.string.participate_text_exercise_synced_changes))
            .assertExists()
    }

    private fun setupUi(testDispatcher: TestDispatcher = UnconfinedTestDispatcher()): TextExerciseParticipationViewModel {
        val viewModel = TextExerciseParticipationViewModel(
            exercise.id!!,
            participation.id!!,
            get(),
            get(),
            get(),
            get(),
            get(),
            testDispatcher
        )

        composeTestRole.setContent {
            TextExerciseParticipationScreen(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
                exercise = exercise,
                onNavigateUp = {}
            )
        }

        return viewModel
    }
}