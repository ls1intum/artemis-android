package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.participate.text_exercise

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.CourseExerciseService
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.StudentParticipation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.SubmissionType
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.TextSubmission
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.R
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.participate.textexercise.SyncState
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.participate.textexercise.TEST_TAG_TEXT_FIELD_PARTICIPATION
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.participate.textexercise.TextExerciseParticipationScreen
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.participate.textexercise.TextExerciseParticipationViewModel
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.service.TextSubmissionService
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.datetime.Clock
import org.junit.Before
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.test.get
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull

@OptIn(ExperimentalTestApi::class)
@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
class TextExerciseParticipationE2eTest : BaseExerciseTest() {

    private lateinit var participation: Participation

    private lateinit var initialSubmission: TextSubmission

    private val textToEnter =
        "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam"

    @Before
    override fun setup() {
        super.setup()
        runBlockingWithTestTimeout {
            val courseExerciseService: CourseExerciseService = get()
            participation =
                courseExerciseService
                    .startExercise(exercise.id!!)
                    .orThrow("Start text exercise participation")
    
            val submissions = participation.submissions
            assertNotNull(submissions, "Submissions are not given in participation")
            assertFalse(submissions.isEmpty(), "Submissions must not be empty")
    
            initialSubmission = assertIs(submissions.first())
        }
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can view already entered text`() {
        val textSubmissionService: TextSubmissionService = get()

        runBlockingWithTestTimeout {
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
            ).orThrow("Could no update text submission to set initial submission")
        }
        

        setupUi()

        composeTestRule
            .onNodeWithTag(TEST_TAG_TEXT_FIELD_PARTICIPATION)
            .assert(hasText(textToEnter))
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can update text by entering new text`() {
        val viewModel = setupUi()

        composeTestRule
            .onNodeWithTag(TEST_TAG_TEXT_FIELD_PARTICIPATION)
            .performTextInput(textToEnter)

        composeTestRule
            .onNodeWithText(context.getString(R.string.participate_text_exercise_submit_button))
            .performClick()

        // Advance time to continue with the syncing
        testDispatcher.scheduler.advanceTimeBy(TextExerciseParticipationViewModel.SyncDelay * 2)

        runBlocking {
            withTimeoutOrNull(DefaultTimeoutMillis) {
                viewModel.syncState.filterIsInstance<SyncState.Synced>().first()
            } ?: throw RuntimeException("State could not be synced in time.")
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.participate_text_exercise_synced_changes))
            .assertExists()
    }

    private fun setupUi(): TextExerciseParticipationViewModel {
        val viewModel = TextExerciseParticipationViewModel(
            exerciseId = exercise.id!!,
            participationId = participation.id!!,
            textSubmissionService = get(),
            textEditorService = get(),
            networkStatusProvider = get(),
            coroutineContext = testDispatcher
        )

        composeTestRule.setContent {
            TextExerciseParticipationScreen(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
                exercise = exercise,
                onNavigateUp = {}
            )
        }

        composeTestRule.waitUntilAtLeastOneExists(
            hasTestTag(TEST_TAG_TEXT_FIELD_PARTICIPATION),
            DefaultTimeoutMillis
        )

        return viewModel
    }
}