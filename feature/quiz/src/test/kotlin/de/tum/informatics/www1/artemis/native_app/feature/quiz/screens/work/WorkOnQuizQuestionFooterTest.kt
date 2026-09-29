package de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.QuizSubmission
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import de.tum.informatics.www1.artemis.native_app.feature.quiz.QuizType
import de.tum.informatics.www1.artemis.native_app.feature.quiz.R
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

/**
 * What the student is told about the state of the quiz below the questions. A server that answers is no way to
 * see the states that say something went wrong.
 */
@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class WorkOnQuizQuestionFooterTest : BaseComposeTest() {

    private var retries = 0

    private val saveFailedText get() = context.getString(R.string.quiz_participation_save_failed)
    private val tryAgainText get() = context.getString(R.string.quiz_participation_save_failed_try_again_button)
    private val neverSavedText get() = context.getString(R.string.quiz_participation_never_saved)
    private val notConnectedText get() = context.getString(R.string.quiz_participation_connection_status_not_connected)

    @Test
    fun `says nothing went wrong when the answers were saved`() {
        setupFooter(latestSavedSubmission = Result.success(QuizSubmission()))

        composeTestRule.onNodeWithText(saveFailedText).assertDoesNotExist()
    }

    @Test
    fun `says that saving failed and saves again on request`() {
        setupFooter(latestSavedSubmission = Result.failure(RuntimeException("Could not save the answers")))

        composeTestRule.onNodeWithText(saveFailedText).assertExists()
        composeTestRule.onNodeWithText(tryAgainText).performClick()

        assertEquals(1, retries)
    }

    @Test
    fun `says that the student is not connected`() {
        setupFooter(isConnected = false)

        composeTestRule.onNodeWithText(notConnectedText).assertExists()
    }

    @Test
    fun `says nothing about the connection while connected`() {
        setupFooter(isConnected = true)

        composeTestRule.onNodeWithText(notConnectedText).assertDoesNotExist()
    }

    @Test
    fun `says that a live quiz was never saved`() {
        setupFooter(quizType = QuizType.Live, lastSubmissionTime = null)

        composeTestRule.onNodeWithText(neverSavedText).assertExists()
    }

    @Test
    fun `says that a practice is one instead of when it was saved`() {
        setupFooter(quizType = QuizType.Practice, lastSubmissionTime = null)

        composeTestRule.onNodeWithText(context.getString(R.string.quiz_participation_practice_mode)).assertExists()
        composeTestRule.onNodeWithText(neverSavedText).assertDoesNotExist()
    }

    private fun setupFooter(
        quizType: QuizType.WorkableQuizType = QuizType.Live,
        lastSubmissionTime: kotlin.time.Instant? = null,
        isConnected: Boolean = true,
        latestSavedSubmission: Result<QuizSubmission>? = null
    ) {
        composeTestRule.setContent {
            // Like the questions screen, which lays the parts of the footer out below each other
            Column(modifier = Modifier.fillMaxWidth()) {
                WorkOnQuizQuestionFooter(
                    modifier = Modifier,
                    quizType = quizType,
                    lastSubmissionTime = lastSubmissionTime,
                    endDate = Clock.System.now() + 10.minutes,
                    isConnected = isConnected,
                    latestSavedSubmission = latestSavedSubmission,
                    clock = Clock.System,
                    canNavigateToPreviousQuestion = false,
                    canNavigateToNextQuestion = false,
                    onRequestPreviousQuestion = {},
                    onRequestNextQuestion = {},
                    onRequestRetrySave = { retries++ }
                )
            }
        }
    }
}
