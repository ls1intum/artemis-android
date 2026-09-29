package de.tum.informatics.www1.artemis.native_app.feature.quiz.screens

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import de.tum.informatics.www1.artemis.native_app.feature.quiz.R
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

/**
 * What a student is told, and offered, while the quiz has not started for them, for the states of a quiz that a
 * server does not produce on demand: attempts that are used up, and the planned start of a synchronized quiz.
 */
@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class WaitForQuizStartScreenTest : BaseComposeTest() {

    private var startedQuizzes = 0

    private val startNowText get() = context.getString(R.string.quiz_participation_wait_for_start_start_now_button)
    private val alreadyParticipatedText get() = context.getString(R.string.quiz_participation_wait_for_start_no_more_attempts_already_participated)
    private val noMoreAttemptsText get() = context.getString(R.string.quiz_participation_wait_for_start_no_more_attempts_no_more_attempts)
    private val timeUntilStartText get() = context.getString(R.string.quiz_participation_wait_for_start_time_until_start)

    @Test
    fun `offers to start an individual quiz while there are attempts left`() {
        setupScreen(QuizExercise(quizMode = QuizExercise.QuizMode.INDIVIDUAL, allowedNumberOfAttempts = 2, remainingNumberOfAttempts = 1))

        composeTestRule.onNodeWithText(startNowText).performClick()

        assertEquals(1, startedQuizzes)
    }

    @Test
    fun `says that the only attempt was used, and offers nothing`() {
        setupScreen(QuizExercise(quizMode = QuizExercise.QuizMode.INDIVIDUAL, allowedNumberOfAttempts = 1, remainingNumberOfAttempts = 0))

        composeTestRule.onNodeWithText(alreadyParticipatedText).assertExists()
        composeTestRule.onNodeWithText(startNowText).assertDoesNotExist()
    }

    @Test
    fun `says that all attempts were used when there were several`() {
        setupScreen(QuizExercise(quizMode = QuizExercise.QuizMode.INDIVIDUAL, allowedNumberOfAttempts = 3, remainingNumberOfAttempts = 0))

        composeTestRule.onNodeWithText(noMoreAttemptsText).assertExists()
        composeTestRule.onNodeWithText(alreadyParticipatedText).assertDoesNotExist()
    }

    @Test
    fun `asks for the passcode of a batch while there are attempts left`() {
        setupScreen(QuizExercise(quizMode = QuizExercise.QuizMode.BATCHED, allowedNumberOfAttempts = 1, remainingNumberOfAttempts = 1))

        composeTestRule.onNodeWithTag(TEST_TAG_TEXT_FIELD_BATCH_PASSWORD).assertExists()
    }

    @Test
    fun `does not ask for a passcode once the attempts of a batched quiz are used`() {
        setupScreen(QuizExercise(quizMode = QuizExercise.QuizMode.BATCHED, allowedNumberOfAttempts = 1, remainingNumberOfAttempts = 0))

        composeTestRule.onNodeWithTag(TEST_TAG_TEXT_FIELD_BATCH_PASSWORD).assertDoesNotExist()
        composeTestRule.onNodeWithText(alreadyParticipatedText).assertExists()
    }

    @Test
    fun `counts down to the planned start of a synchronized quiz`() {
        val start = Clock.System.now() + 5.minutes
        setupScreen(
            QuizExercise(quizMode = QuizExercise.QuizMode.SYNCHRONIZED),
            batch = QuizExercise.QuizBatch(id = 1, startTime = start, started = false)
        )

        composeTestRule.onNodeWithText(timeUntilStartText).assertExists()
        composeTestRule.onNodeWithText(startNowText).assertDoesNotExist()
    }

    @Test
    fun `does not count down when the start of a synchronized quiz is not planned`() {
        setupScreen(QuizExercise(quizMode = QuizExercise.QuizMode.SYNCHRONIZED))

        composeTestRule.onNodeWithText(timeUntilStartText).assertDoesNotExist()
        composeTestRule.onNodeWithText(context.getString(R.string.quiz_participation_wait_for_start_explanation)).assertExists()
    }

    private fun setupScreen(exercise: QuizExercise, batch: QuizExercise.QuizBatch? = null) {
        composeTestRule.setContent {
            WaitForQuizStartScreen(
                modifier = Modifier,
                exercise = exercise,
                isConnected = true,
                isStartingOrJoiningQuiz = false,
                batch = batch,
                clock = Clock.System,
                onRequestRefresh = {},
                onClickJoinBatch = {},
                onClickStartQuiz = { startedQuizzes++ }
            )
        }
    }
}
