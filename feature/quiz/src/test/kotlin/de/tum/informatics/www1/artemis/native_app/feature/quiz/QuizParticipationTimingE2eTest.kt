package de.tum.informatics.www1.artemis.native_app.feature.quiz

import android.os.Looper
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.common.test.testServerUrl
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.MultipleChoiceSubmittedAnswer
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.ShortAnswerSubmittedAnswer
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createQuiz
import de.tum.informatics.www1.artemis.native_app.feature.login.test.getAdminAccessToken
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizParticipationScreen
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work.TEST_TAG_WORK_ON_QUIZ_QUESTIONS_SCREEN
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

/**
 * How the app saves the answers of a live quiz while time passes, and what happens when the time is up.
 *
 * The app waits with the timers of the main looper. The tests move that clock forward by exact amounts, instead
 * of waiting: how far the clock moves while a test waits depends on how busy the machine is, and a test that
 * waits for 30 seconds would pass or fail with it. The app works out how much of the quiz is left from the
 * time of the server, and that time is real, so the quizzes are long enough for the seconds a test takes.
 *
 * The app saves at most every 30 seconds while the answers change, and right away during the last 10 seconds
 * of the quiz.
 */
@OptIn(ExperimentalTestApi::class)
@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
internal class QuizParticipationTimingE2eTest : QuizBaseE2eTest(QuizType.Live, useRealWebsocket = true) {

    private val correctOptionText = "Enter a correct answer option here"
    private val neverSavedText get() = context.getString(R.string.quiz_participation_never_saved)

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `saves at most every 30 seconds while the answers change`() {
        val quiz = startQuiz(durationInSeconds = 600)
        openQuiz(quiz)

        composeTestRule.onNodeWithText(correctOptionText).performClick()
        waitUntilSaved()
        assertEquals(1, savedAnswers(quiz).size, "The first change should be saved right away")

        // A second change comes soon after the first save. It has to wait for the interval to pass
        secondChange()

        passTime(20.seconds)
        assertEquals(1, savedAnswers(quiz).size, "The second change was saved before the interval had passed")

        passTime(12.seconds)
        awaitSavedAnswers(quiz, count = 2)
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `saves right away when the end of the quiz is near`() {
        // The interval would be 30 seconds, which is as long as the quiz
        val quiz = startQuiz(durationInSeconds = 30)
        val quizEnd = endOf(quiz)
        openQuiz(quiz)

        composeTestRule.onNodeWithText(correctOptionText).performClick()
        waitUntilSaved()
        // When the first save is done, the app waits until 10 seconds before the end of the quiz
        val expectedWait = (quizEnd - System.currentTimeMillis()) / 1000.0 - 10
        assertTrue(expectedWait in 6.0..25.0, "The test has to reach the first save within a few seconds of the start of the quiz")

        secondChange()

        passTime((expectedWait - 4).seconds)
        assertEquals(1, savedAnswers(quiz).size, "The second change was saved earlier than 10 seconds before the end")

        passTime(8.seconds)
        awaitSavedAnswers(quiz, count = 2)
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `ends the quiz for the student when the time is up`() {
        val quiz = startQuiz(durationInSeconds = 600)
        openQuiz(quiz)

        composeTestRule.onNodeWithText("Deadline:", substring = true).assertExists()
        passTime(590.seconds)
        composeTestRule.onNodeWithTag(TEST_TAG_WORK_ON_QUIZ_QUESTIONS_SCREEN).assertExists()

        passTime(20.seconds)
        composeTestRule.waitUntil(DefaultTimeoutMillis) {
            composeTestRule.waitForIdle()
            composeTestRule.onAllNodes(hasTestTag(TEST_TAG_WORK_ON_QUIZ_QUESTIONS_SCREEN)).fetchSemanticsNodes().isEmpty()
        }
        composeTestRule.onNodeWithText(context.getString(R.string.quiz_participation_submit_button)).assertDoesNotExist()
    }

    private fun secondChange() {
        composeTestRule.onNodeWithText(context.getString(R.string.quiz_participation_button_next_question)).performClick()
        composeTestRule.onAllNodes(hasSetTextAction())[0].performTextInput("is")
        composeTestRule.waitForIdle()
    }

    /**
     * Moves the clocks the timers of the app run on forward: the one of the main looper, which the view model
     * waits with, and the one of the compose test, which what the screen waits for in its composition runs on
     */
    private fun passTime(duration: Duration) {
        shadowOf(Looper.getMainLooper()).idleFor(duration.toJavaDuration())
        composeTestRule.mainClock.advanceTimeBy(duration.inWholeMilliseconds)
        composeTestRule.waitForIdle()
        // What the app started because of it, like an upload, happens off the main looper
        Thread.sleep(700)
        composeTestRule.waitForIdle()
    }

    private fun startQuiz(durationInSeconds: Int): QuizExercise = runBlockingWithTestTimeout {
        val quiz = createQuiz(getAdminAccessToken(), courseId, randomizeQuestionOrder = false, durationInSeconds = durationInSeconds)
        participationService.findParticipation(quiz.id).orThrow("Could not start quiz participation")
        quizExerciseService.join(quiz.id, "", testServerUrl, accessToken).orThrow("Could not join the quiz")
        quiz
    }

    /**
     * When the quiz ends for the student, in the time of this machine
     */
    private fun endOf(quiz: QuizExercise): Long = runBlockingWithTestTimeout {
        val started = participationService.findParticipation(quiz.id).orThrow("Could not load the participation").exercise as QuizExercise
        val batch = assertNotNull(started.quizBatches.orEmpty().firstOrNull(), "The student has no batch")

        assertNotNull(batch.startTime).toEpochMilliseconds() + quiz.duration!! * 1000L
    }

    private fun openQuiz(quiz: QuizExercise) {
        setupUi(quiz.id) { viewModel ->
            QuizParticipationScreen(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
                onNavigateToInspectResult = {},
                onNavigateUp = {}
            )
        }

        composeTestRule.waitUntilExactlyOneExists(hasTestTag(TEST_TAG_WORK_ON_QUIZ_QUESTIONS_SCREEN), DefaultTimeoutMillis)
    }

    private fun waitUntilSaved() {
        composeTestRule.waitUntil(DefaultTimeoutMillis) {
            composeTestRule.waitForIdle()
            composeTestRule.onAllNodes(hasText(neverSavedText)).fetchSemanticsNodes().isEmpty()
        }
    }

    /**
     * Waits for an upload that has been started. The clocks of the app stand still while this waits, unlike while
     * the compose test waits: then the upload could as well be one that only happens later.
     */
    private fun awaitSavedAnswers(quiz: QuizExercise, count: Int) {
        val giveUp = System.currentTimeMillis() + 10_000
        while (savedAnswers(quiz).size != count && System.currentTimeMillis() < giveUp) {
            Thread.sleep(100)
        }
        assertEquals(count, savedAnswers(quiz).size, "The answers were not saved after the time had passed")
    }

    /**
     * The answers the server holds that are not empty
     */
    private fun savedAnswers(quiz: QuizExercise): List<Any> = runBlockingWithTestTimeout {
        val submission = participationService.findParticipation(quiz.id).orThrow("Could not load the participation")
            .quizSubmission
        assertNotNull(submission, "The server holds no submission")

        submission.submittedAnswers.filter { answer ->
            when (answer) {
                is MultipleChoiceSubmittedAnswer -> answer.selectedOptions.isNotEmpty()
                is ShortAnswerSubmittedAnswer -> answer.submittedTexts.any { !it.text.isNullOrBlank() }
                else -> false
            }
        }
    }
}
