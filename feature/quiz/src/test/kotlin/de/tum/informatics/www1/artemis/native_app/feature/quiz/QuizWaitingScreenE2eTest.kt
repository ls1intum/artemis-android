package de.tum.informatics.www1.artemis.native_app.feature.quiz

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.addQuizExerciseBatch
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createQuiz
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.startQuizExerciseBatch
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.startQuizExerciseNow
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work.TEST_TAG_WORK_ON_QUIZ_QUESTIONS_SCREEN
import de.tum.informatics.www1.artemis.native_app.feature.login.test.getAdminAccessToken
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizParticipationUi
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.TEST_TAG_TEXT_FIELD_BATCH_PASSWORD
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.TEST_TAG_WAIT_FOR_QUIZ_START_SCREEN
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertNotNull

@OptIn(ExperimentalTestApi::class)
@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
internal class QuizWaitingScreenE2eTest : QuizBaseE2eTest(QuizType.Live, useRealWebsocket = true) {

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can start individual quiz`() {
        val quiz: QuizExercise = runBlockingWithTestTimeout {
            createQuiz(getAdminAccessToken(), courseId, QuizExercise.QuizMode.INDIVIDUAL)
        }

        setupUi(quiz.id) { viewModel ->
            QuizParticipationUi(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
                onNavigateToInspectResult = {},
                onNavigateUp = {}
            )
        }

        composeTestRule.waitUntilExactlyOneExists(
            hasTestTag(TEST_TAG_WAIT_FOR_QUIZ_START_SCREEN),
            DefaultTimeoutMillis
        )

        composeTestRule
            .onNodeWithText(context.getString(R.string.quiz_participation_wait_for_start_start_now))
            .assertExists("start now text missing")

        composeTestRule
            .onNodeWithText(context.getString(R.string.quiz_participation_wait_for_start_start_now_button))
            .performClick()

        // The waiting screen gives way to the questions
        composeTestRule.waitUntilExactlyOneExists(
            hasTestTag(TEST_TAG_WORK_ON_QUIZ_QUESTIONS_SCREEN),
            DefaultTimeoutMillis
        )
        composeTestRule.onNodeWithTag(TEST_TAG_WAIT_FOR_QUIZ_START_SCREEN).assertDoesNotExist()
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can start batched quiz`() {
        val (quiz, batch) = runBlockingWithTestTimeout {
            val quiz = createQuiz(getAdminAccessToken(), courseId, QuizExercise.QuizMode.BATCHED)

            val batch = addQuizExerciseBatch(getAdminAccessToken(), quiz.id)

            quiz to batch

        }

        setupUi(quiz.id) { viewModel ->
            QuizParticipationUi(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
                onNavigateToInspectResult = {},
                onNavigateUp = {}
            )
        }


        composeTestRule.waitUntilExactlyOneExists(
            hasTestTag(TEST_TAG_WAIT_FOR_QUIZ_START_SCREEN),
            DefaultTimeoutMillis
        )

        composeTestRule
            .onNodeWithTag(TEST_TAG_TEXT_FIELD_BATCH_PASSWORD)
            .performTextInput(assertNotNull(batch.password, "Batch password is null"))

        composeTestRule
            .onNodeWithText(context.getString(R.string.quiz_participation_wait_for_start_join_button))
            .performClick()

        // Joined: the student now waits for the instructor
        composeTestRule
            .waitUntilExactlyOneExists(
                hasText(context.getString(R.string.quiz_participation_wait_for_start_explanation)),
                DefaultTimeoutMillis
            )
        composeTestRule.onNodeWithTag(TEST_TAG_WORK_ON_QUIZ_QUESTIONS_SCREEN).assertDoesNotExist()

        // The instructor starts the batch, and Artemis lets the waiting student know
        runBlockingWithTestTimeout {
            startQuizExerciseBatch(getAdminAccessToken(), batch)
        }

        composeTestRule.waitUntilExactlyOneExists(
            hasTestTag(TEST_TAG_WORK_ON_QUIZ_QUESTIONS_SCREEN),
            DefaultTimeoutMillis
        )
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `a wrong passcode does not join the batch`() {
        val quiz = runBlockingWithTestTimeout {
            createQuiz(getAdminAccessToken(), courseId, QuizExercise.QuizMode.BATCHED).also {
                addQuizExerciseBatch(getAdminAccessToken(), it.id)
            }
        }

        setupUi(quiz.id) { viewModel ->
            QuizParticipationUi(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
                onNavigateToInspectResult = {},
                onNavigateUp = {}
            )
        }

        composeTestRule.waitUntilExactlyOneExists(hasTestTag(TEST_TAG_WAIT_FOR_QUIZ_START_SCREEN), DefaultTimeoutMillis)

        composeTestRule.onNodeWithTag(TEST_TAG_TEXT_FIELD_BATCH_PASSWORD).performTextInput("not the passcode")
        composeTestRule.onNodeWithText(context.getString(R.string.quiz_participation_wait_for_start_join_button)).performClick()

        composeTestRule.waitUntilExactlyOneExists(
            hasText(context.getString(R.string.quiz_participation_wait_for_start_join_batch_error_message)),
            DefaultTimeoutMillis
        )
        composeTestRule.onNodeWithText(context.getString(R.string.quiz_participation_wait_for_start_explanation)).assertDoesNotExist()
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `a synchronized quiz starts when the instructor starts it`() {
        val quiz = runBlockingWithTestTimeout {
            createQuiz(getAdminAccessToken(), courseId, QuizExercise.QuizMode.SYNCHRONIZED)
        }

        setupUi(quiz.id) { viewModel ->
            QuizParticipationUi(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
                onNavigateToInspectResult = {},
                onNavigateUp = {}
            )
        }

        composeTestRule.waitUntilExactlyOneExists(hasTestTag(TEST_TAG_WAIT_FOR_QUIZ_START_SCREEN), DefaultTimeoutMillis)
        composeTestRule.onNodeWithText(context.getString(R.string.quiz_participation_wait_for_start_start_now_button)).assertDoesNotExist()
        composeTestRule.onNodeWithTag(TEST_TAG_TEXT_FIELD_BATCH_PASSWORD).assertDoesNotExist()

        runBlockingWithTestTimeout {
            startQuizExerciseNow(getAdminAccessToken(), quiz.id)
        }

        composeTestRule.waitUntilExactlyOneExists(
            hasTestTag(TEST_TAG_WORK_ON_QUIZ_QUESTIONS_SCREEN),
            DefaultTimeoutMillis
        )
    }
}
