package de.tum.informatics.www1.artemis.native_app.feature.quiz

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.common.test.testServerUrl
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.MultipleChoiceSubmittedAnswer
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createQuiz
import de.tum.informatics.www1.artemis.native_app.feature.login.test.getAdminAccessToken
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizParticipationScreen
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work.TEST_TAG_WORK_ON_QUIZ_QUESTIONS_SCREEN
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.core.module.Module
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import java.net.URI
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * What the student sees when the connection to Artemis is really lost, and how the quiz carries on when it is
 * back. The app reaches Artemis through a [SeveringProxy] that a test cuts, so that the websocket is closed and
 * every request fails, as when the network is gone, and the app has to find its way back on its own.
 */
@OptIn(ExperimentalTestApi::class)
@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
internal class QuizParticipationConnectionE2eTest : QuizBaseE2eTest(QuizType.Live, useRealWebsocket = true) {

    private val proxy = SeveringProxy(URI(testServerUrl).host, URI(testServerUrl).port)

    private lateinit var quiz: QuizExercise
    private var navigatedUp = false

    private val correctOptionText = "Enter a correct answer option here"
    private val notConnectedText get() = context.getString(R.string.quiz_participation_connection_status_not_connected)
    private val saveFailedText get() = context.getString(R.string.quiz_participation_save_failed)
    private val tryAgainText get() = context.getString(R.string.quiz_participation_save_failed_try_again_button)
    private val neverSavedText get() = context.getString(R.string.quiz_participation_never_saved)

    override fun overrideModules(): List<Module> = listOf(
        module {
            single<ServerConfigurationService> {
                object : ServerConfigurationService {
                    override val serverUrl = flowOf(proxy.url)

                    override suspend fun updateServerUrl(serverUrl: String) = Unit
                }
            }
        }
    )

    override suspend fun setupHook() {
        super.setupHook()

        quiz = createQuiz(getAdminAccessToken(), courseId, randomizeQuestionOrder = false)
        participationService.findParticipation(quiz.id).orThrow("Could not start quiz participation")
        quizExerciseService.join(quiz.id, "", proxy.url, accessToken).orThrow("Could not join the quiz")
    }

    @After
    fun closeProxy() = proxy.close()

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `says when the connection is lost and when it is back`() {
        openQuiz()
        awaitConnected()
        val connectionsBefore = proxy.acceptedConnections
        assertTrue(connectionsBefore > 0, "The app should reach the server through the proxy")

        proxy.sever()
        composeTestRule.waitUntilExactlyOneExists(hasText(notConnectedText), DefaultTimeoutMillis)

        proxy.restore()
        awaitConnected()
        assertTrue(proxy.acceptedConnections > connectionsBefore, "The app should have connected again through the proxy")
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `keeps an answer that could not be saved and saves it when the student asks again`() {
        openQuiz()
        awaitConnected()

        proxy.sever()
        composeTestRule.onNodeWithText(correctOptionText).performClick()
        composeTestRule.waitUntilExactlyOneExists(hasText(saveFailedText), DefaultTimeoutMillis)
        composeTestRule.onNodeWithText(neverSavedText).assertExists()

        // Trying again while the connection is still gone changes nothing
        composeTestRule.onNodeWithText(tryAgainText).performClick()
        composeTestRule.waitUntilExactlyOneExists(hasText(saveFailedText), DefaultTimeoutMillis)

        proxy.restore()
        awaitConnected()
        composeTestRule.onNodeWithText(tryAgainText).performClick()

        composeTestRule.waitUntil(DefaultTimeoutMillis) {
            composeTestRule.waitForIdle()
            composeTestRule.onAllNodes(hasText(saveFailedText)).fetchSemanticsNodes().isEmpty()
        }
        composeTestRule.onNodeWithText(neverSavedText).assertDoesNotExist()

        val submission = savedSubmission()
        assertEquals(false, submission.submitted, "Saving must not submit the quiz")
        val answer = submission.submittedAnswers.filterIsInstance<MultipleChoiceSubmittedAnswer>().single()
        assertEquals(listOf(correctOptionText), answer.selectedOptions.map { it.text })
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `warns that unsaved changes are lost when leaving after a failed save`() {
        openQuiz()
        awaitConnected()

        proxy.sever()
        composeTestRule.onNodeWithText(correctOptionText).performClick()
        composeTestRule.waitUntilExactlyOneExists(hasText(saveFailedText), DefaultTimeoutMillis)

        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule
            .onNode(hasAnyAncestor(isDialog()) and hasText("unsaved changes", substring = true))
            .assertExists()

        composeTestRule
            .onNode(hasAnyAncestor(isDialog()) and hasText(context.getString(R.string.quiz_participation_leave_without_submit_dialog_negative)))
            .performClick()
        assertTrue(!navigatedUp, "Stayed in the quiz, but the screen was left")
        composeTestRule.onNodeWithTag(TEST_TAG_WORK_ON_QUIZ_QUESTIONS_SCREEN).assertExists()

        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule
            .onNode(hasAnyAncestor(isDialog()) and hasText(context.getString(R.string.quiz_participation_leave_without_submit_dialog_positive)))
            .performClick()
        assertTrue(navigatedUp, "Left the quiz, but the screen was not left")
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `does not talk about unsaved changes when leaving after a save that worked`() {
        openQuiz()
        awaitConnected()

        composeTestRule.onNodeWithText(correctOptionText).performClick()
        composeTestRule.waitUntil(DefaultTimeoutMillis) {
            composeTestRule.waitForIdle()
            composeTestRule.onAllNodes(hasText(neverSavedText)).fetchSemanticsNodes().isEmpty()
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        composeTestRule.onNode(hasAnyAncestor(isDialog()) and hasText("unsaved changes", substring = true)).assertDoesNotExist()
        composeTestRule.onNode(hasAnyAncestor(isDialog()) and hasText("You have not submitted yet", substring = true)).assertExists()
    }

    private fun openQuiz() {
        setupUi(quiz.id) { viewModel ->
            QuizParticipationScreen(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
                onNavigateToInspectResult = {},
                onNavigateUp = { navigatedUp = true }
            )
        }

        composeTestRule.waitUntilExactlyOneExists(hasTestTag(TEST_TAG_WORK_ON_QUIZ_QUESTIONS_SCREEN), DefaultTimeoutMillis)
    }

    /**
     * The websocket is connected once the footer no longer says that the student is not
     */
    private fun awaitConnected() {
        composeTestRule.waitUntil(DefaultTimeoutMillis) {
            composeTestRule.waitForIdle()
            composeTestRule.onAllNodes(hasText(notConnectedText)).fetchSemanticsNodes().isEmpty()
        }
    }

    private fun savedSubmission() = runBlockingWithTestTimeout {
        participationService.findParticipation(quiz.id).orThrow("Could not load the participation").quizSubmission
            ?: error("The server holds no submission")
    }
}
