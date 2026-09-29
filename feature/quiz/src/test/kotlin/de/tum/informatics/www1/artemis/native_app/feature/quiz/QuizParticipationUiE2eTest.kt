package de.tum.informatics.www1.artemis.native_app.feature.quiz

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.common.test.testServerUrl
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.DragAndDropQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.MultipleChoiceQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.ShortAnswerQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.QuizSubmission
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.DragAndDropSubmittedAnswer
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.MultipleChoiceSubmittedAnswer
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.ShortAnswerSubmittedAnswer
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createQuiz
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.endQuizExerciseNow
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.LocalArtemisImageProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.impl.ArtemisImageProviderImpl
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.impl.BaseImageProviderImpl
import de.tum.informatics.www1.artemis.native_app.feature.login.test.getAdminAccessToken
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizParticipationScreen
import de.tum.informatics.www1.artemis.native_app.feature.quiz.question.draganddrop.body.work_area.getTestTagForDropLocation
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work.TEST_TAG_WORK_ON_QUIZ_QUESTIONS_SCREEN
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.test.get
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Takes part in a live quiz the way a student does: through the screen, by tapping and typing, not by calling
 * what the screen would call. The quiz has its questions in a fixed order: multiple choice, short answer, and
 * drag and drop last.
 */
@OptIn(ExperimentalTestApi::class)
@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
internal class QuizParticipationUiE2eTest : QuizBaseE2eTest(QuizType.Live, useRealWebsocket = true) {

    private lateinit var quiz: QuizExercise

    private var navigatedUp = false

    private var inspectedResult: QuizType.ViewableQuizType? = null

    private val correctOptionText = "Enter a correct answer option here"

    private val submitText get() = context.getString(R.string.quiz_participation_submit_button)
    private val nextText get() = context.getString(R.string.quiz_participation_button_next_question)
    private val previousText get() = context.getString(R.string.quiz_participation_button_previous_question)

    override suspend fun setupHook() {
        super.setupHook()

        quiz = createQuiz(getAdminAccessToken(), courseId, randomizeQuestionOrder = false)

        // Like the app, start the participation before joining, see QuizParticipationE2eTest
        participationService.findParticipation(quiz.id).orThrow("Could not start quiz participation")
        quizExerciseService.join(quiz.id, "", testServerUrl, accessToken).orThrow("Could not join the quiz")
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `tapping an option is uploaded with the submission`() {
        openQuiz()

        composeTestRule.onNodeWithText(correctOptionText).performClick()
        submitThroughDialog()

        val submission = submissionOnServer()
        assertEquals(true, submission.submitted)
        val answer = submission.answerTo<MultipleChoiceSubmittedAnswer>(MultipleChoiceQuizQuestion::class.java.simpleName)
        assertEquals(listOf(correctOptionText), answer.selectedOptions.map { it.text })
        assertEquals(setOf("multiple choice"), submission.answeredKinds(), "Only the question that was answered should hold an answer")
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `tapping a selected option takes the answer back`() {
        openQuiz()

        composeTestRule.onNodeWithText(correctOptionText).performClick()
        composeTestRule.onNodeWithText(correctOptionText).performClick()
        submitThroughDialog()

        val submission = submissionOnServer()
        assertEquals(true, submission.submitted)
        assertEquals(emptyList(), submission.answerTo<MultipleChoiceSubmittedAnswer>(MultipleChoiceQuizQuestion::class.java.simpleName).selectedOptions)
        assertEquals(emptySet(), submission.answeredKinds(), "Nothing was answered in the end")
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `typing into a spot is uploaded with the submission`() {
        openQuiz()

        composeTestRule.onNodeWithText(nextText).performClick()
        composeTestRule.onAllNodes(hasSetTextAction())[0].performTextInput("is")
        submitThroughDialog()

        val submission = submissionOnServer()
        assertEquals(true, submission.submitted)
        assertEquals(setOf("short answer"), submission.answeredKinds())
        val answer = submission.answerTo<ShortAnswerSubmittedAnswer>(ShortAnswerQuizQuestion::class.java.simpleName)
        assertEquals(
            listOf(1 to "is"),
            answer.submittedTexts.map { it.spot?.spotNr to it.text }.map { (spot, text) -> spot!! to text!! }
        )
    }

    // The drag and drop question needs room for its background image and the items to drag, and the image
    // needs the native graphics of Robolectric to be decoded
    @Config(qualifiers = "w411dp-h1200dp")
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    @Test(timeout = DefaultTestTimeoutMillis)
    fun `dragging an item onto a drop location is uploaded with the submission`() {
        val question = quiz.quizQuestions.filterIsInstance<DragAndDropQuizQuestion>().single()
        val item = question.dragItems.first { it.text == "item1" }
        val dropLocation = question.dropLocations.first()

        openQuiz()
        composeTestRule.onNodeWithText("DD").performClick()

        // The drop locations show once the background image has been loaded from Artemis
        composeTestRule.waitUntil(DefaultTimeoutMillis) {
            composeTestRule.waitForIdle()
            composeTestRule.onAllNodes(hasTestTag(getTestTagForDropLocation(dropLocation.id))).fetchSemanticsNodes().isNotEmpty()
        }

        dragOnto("item1", getTestTagForDropLocation(dropLocation.id))
        submitThroughDialog()

        val submission = submissionOnServer()
        assertEquals(true, submission.submitted)
        assertEquals(setOf("drag and drop"), submission.answeredKinds())
        val answer = submission.answerTo<DragAndDropSubmittedAnswer>(DragAndDropQuizQuestion::class.java.simpleName)
        assertEquals(
            listOf(item.id to dropLocation.id),
            answer.mappings.map { it.dragItem?.id to it.dropLocation?.id }.map { (drag, drop) -> drag!! to drop!! }
        )
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `moves between the questions with next and previous`() {
        openQuiz()

        composeTestRule.onNodeWithText(previousText).assertIsNotEnabled()
        composeTestRule.onNodeWithText(nextText).assertIsEnabled()
        composeTestRule.onNodeWithText(questionTitle(0, "MC1")).assertExists()

        composeTestRule.onNodeWithText(nextText).performClick()
        composeTestRule.onNodeWithText(questionTitle(1, "ShortAnswer")).assertExists()
        composeTestRule.onNodeWithText(previousText).assertIsEnabled()
        composeTestRule.onNodeWithText(nextText).assertIsEnabled()

        composeTestRule.onNodeWithText(nextText).performClick()
        composeTestRule.onNodeWithText(questionTitle(2, "Dnd Question")).assertExists()
        composeTestRule.onNodeWithText(nextText).assertIsNotEnabled()

        composeTestRule.onNodeWithText(previousText).performClick()
        composeTestRule.onNodeWithText(questionTitle(1, "ShortAnswer")).assertExists()
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `answers stay when moving away from a question and back`() {
        openQuiz()

        composeTestRule.onNodeWithText(correctOptionText).performClick()
        composeTestRule.onNodeWithText(nextText).performClick()
        composeTestRule.onNodeWithText(previousText).performClick()
        submitThroughDialog()

        val submission = submissionOnServer()
        assertEquals(true, submission.submitted)
        assertEquals(setOf("multiple choice"), submission.answeredKinds())
        val answer = submission.answerTo<MultipleChoiceSubmittedAnswer>(MultipleChoiceQuizQuestion::class.java.simpleName)
        assertEquals(listOf(correctOptionText), answer.selectedOptions.map { it.text })
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `saves an answer while the quiz runs, without submitting it`() {
        openQuiz()
        composeTestRule.onNodeWithText(context.getString(R.string.quiz_participation_never_saved)).assertExists()

        composeTestRule.onNodeWithText(correctOptionText).performClick()

        // The footer says so when the server has taken the answer
        composeTestRule.waitUntil(DefaultTimeoutMillis) {
            composeTestRule.waitForIdle()
            composeTestRule
                .onAllNodes(hasText(context.getString(R.string.quiz_participation_never_saved)))
                .fetchSemanticsNodes().isEmpty()
        }

        val submission = submissionOnServer()
        assertEquals(false, submission.submitted, "Saving must not submit the quiz")
        val answer = submission.answerTo<MultipleChoiceSubmittedAnswer>(MultipleChoiceQuizQuestion::class.java.simpleName)
        assertEquals(listOf(correctOptionText), answer.selectedOptions.map { it.text })
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `warns when submitting without having answered every question`() {
        openQuiz()
        // Two of the three questions are answered: that is still not all of them
        composeTestRule.onNodeWithText(correctOptionText).performClick()
        composeTestRule.onNodeWithText("SA").performClick()
        composeTestRule.onAllNodes(hasSetTextAction())[0].performTextInput("is")

        composeTestRule.onNodeWithText(submitText).performClick()

        composeTestRule.onNode(hasAnyAncestor(isDialog()) and hasText("not answered all questions", substring = true)).assertExists()
        composeTestRule.onNode(hasAnyAncestor(isDialog()) and hasText(context.getString(R.string.quiz_participation_submit_dialog_negative))).assertExists()
    }

    // The drag and drop question needs room for its background image and the items to drag, and the image
    // needs the native graphics of Robolectric to be decoded
    @Config(qualifiers = "w411dp-h1200dp")
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    @Test(timeout = DefaultTestTimeoutMillis)
    fun `does not warn once every question is answered`() {
        val dropLocation = quiz.quizQuestions.filterIsInstance<DragAndDropQuizQuestion>().single().dropLocations.first()

        openQuiz()
        composeTestRule.onNodeWithText(correctOptionText).performClick()
        composeTestRule.onNodeWithText("SA").performClick()
        composeTestRule.onAllNodes(hasSetTextAction())[0].performTextInput("is")
        composeTestRule.onNodeWithText("DD").performClick()
        composeTestRule.waitUntil(DefaultTimeoutMillis) {
            composeTestRule.waitForIdle()
            composeTestRule.onAllNodes(hasTestTag(getTestTagForDropLocation(dropLocation.id))).fetchSemanticsNodes().isNotEmpty()
        }
        dragOnto("item1", getTestTagForDropLocation(dropLocation.id))

        composeTestRule.onNodeWithText(submitText).performClick()

        composeTestRule.onNode(hasAnyAncestor(isDialog()) and hasText("not answered all questions", substring = true)).assertDoesNotExist()
        composeTestRule.onNode(hasAnyAncestor(isDialog()) and hasText("Are you sure you want to submit", substring = true)).assertExists()
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `cancelling the submit dialog leaves the quiz open and unsubmitted`() {
        openQuiz()
        composeTestRule.onNodeWithText(correctOptionText).performClick()

        composeTestRule.onNodeWithText(submitText).performClick()
        composeTestRule
            .onNode(hasAnyAncestor(isDialog()) and hasText(context.getString(R.string.quiz_participation_submit_dialog_negative)))
            .performClick()

        composeTestRule.onNodeWithTag(TEST_TAG_WORK_ON_QUIZ_QUESTIONS_SCREEN).assertExists()
        assertEquals(false, submissionOnServer().submitted)
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `asks before leaving a quiz that was not submitted`() {
        openQuiz()

        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule
            .onNode(hasAnyAncestor(isDialog()) and hasText(context.getString(R.string.quiz_participation_leave_without_submit_dialog_negative)))
            .performClick()
        assertTrue(!navigatedUp, "Stayed in the quiz, but the screen was left")

        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule
            .onNode(hasAnyAncestor(isDialog()) and hasText(context.getString(R.string.quiz_participation_leave_without_submit_dialog_positive)))
            .performClick()
        assertTrue(navigatedUp, "Left the quiz, but the screen was not left")
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `moves on to the result when the quiz ends while the student works`() {
        openQuiz()
        composeTestRule.onNodeWithText(correctOptionText).performClick()
        composeTestRule.waitUntil(DefaultTimeoutMillis) {
            composeTestRule.waitForIdle()
            composeTestRule.onAllNodes(hasText(context.getString(R.string.quiz_participation_never_saved))).fetchSemanticsNodes().isEmpty()
        }

        // The instructor ends the quiz. Artemis scores it and pushes the result to the student, who is still in the quiz
        runBlockingWithTestTimeout { endQuizExerciseNow(getAdminAccessToken(), quiz.id) }

        composeTestRule.waitUntil(DefaultTimeoutMillis) {
            composeTestRule.waitForIdle()
            inspectedResult == QuizType.ViewResults
        }

        // The student's answer was in before the instructor ended the quiz, so it is what is scored: 1 of 3 points
        val participation = runBlockingWithTestTimeout { participationService.findParticipation(quiz.id).orThrow("Could not load the participation") }
        val result = assertNotNull(participation.quizResult, "The server holds no result for the quiz")
        assertEquals(1.0, participation.quizSubmission?.scoreInPoints)
        assertEquals(true, result.rated)
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `shows the hint of a question and of an answer option on request`() {
        val question = quiz.quizQuestions.filterIsInstance<MultipleChoiceQuizQuestion>().single()
        val questionHint = assertNotNull(question.hint)
        val optionHint = assertNotNull(question.answerOptions.first { it.text == correctOptionText }.hint)
        openQuiz()

        composeTestRule.onNodeWithText(questionHint).assertDoesNotExist()
        composeTestRule.onNodeWithText(context.getString(R.string.quiz_participation_question_header_hint_button)).performClick()
        composeTestRule.onNode(hasAnyAncestor(isDialog()) and hasText(questionHint)).assertExists()
        composeTestRule.onNode(hasAnyAncestor(isDialog()) and hasText(context.getString(android.R.string.ok))).performClick()
        composeTestRule.onNodeWithText(questionHint).assertDoesNotExist()

        composeTestRule
            .onNodeWithContentDescription(context.getString(R.string.quiz_participation_multiple_choice_help_button_content_description))
            .performClick()
        composeTestRule.onNode(hasAnyAncestor(isDialog()) and hasText(optionHint)).assertExists()
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `leaves a quiz that was submitted with the button of the ended screen`() {
        runBlockingWithTestTimeout {
            quizParticipationService.submitForLiveMode(QuizSubmission(submitted = true), quiz.id, testServerUrl, accessToken)
                .orThrow("Could not submit the quiz")
        }
        openQuiz(waitForQuestions = false)

        composeTestRule.waitUntilExactlyOneExists(
            hasText(context.getString(R.string.quiz_participation_quiz_ended_leave_button), substring = true),
            DefaultTimeoutMillis
        )
        assertTrue(!navigatedUp)
        composeTestRule.onNodeWithText(context.getString(R.string.quiz_participation_quiz_ended_leave_button), substring = true).performClick()
        assertTrue(navigatedUp, "The button of the ended screen did not leave the quiz")
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `a submitted quiz cannot be worked on again`() {
        runBlockingWithTestTimeout {
            quizParticipationService.submitForLiveMode(QuizSubmission(submitted = true), quiz.id, testServerUrl, accessToken)
                .orThrow("Could not submit the quiz")
        }

        setupUi(quiz.id) { viewModel ->
            QuizParticipationScreen(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
                onNavigateToInspectResult = {},
                onNavigateUp = { navigatedUp = true }
            )
        }

        composeTestRule.waitUntilExactlyOneExists(
            hasText(context.getString(R.string.quiz_participation_quiz_ended_leave_button), substring = true),
            DefaultTimeoutMillis
        )
        composeTestRule.onNodeWithText(submitText).assertDoesNotExist()
        composeTestRule.onNodeWithTag(TEST_TAG_WORK_ON_QUIZ_QUESTIONS_SCREEN).assertDoesNotExist()
    }

    private fun openQuiz(waitForQuestions: Boolean = true) {
        // The images of the app come from Artemis, with the login of the student
        val imageProvider = ArtemisImageProviderImpl(get<ArtemisContextProvider>(), BaseImageProviderImpl())

        setupUi(quiz.id) { viewModel ->
            CompositionLocalProvider(LocalArtemisImageProvider provides imageProvider) {
                QuizParticipationScreen(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    onNavigateToInspectResult = { inspectedResult = it },
                    onNavigateUp = { navigatedUp = true }
                )
            }
        }

        if (waitForQuestions) {
            composeTestRule.waitUntilExactlyOneExists(
                hasTestTag(TEST_TAG_WORK_ON_QUIZ_QUESTIONS_SCREEN),
                DefaultTimeoutMillis
            )
        }
    }

    private fun submitThroughDialog() {
        composeTestRule.onNodeWithText(submitText).performClick()
        composeTestRule
            .onNode(hasAnyAncestor(isDialog()) and hasText(context.getString(R.string.quiz_participation_submit_dialog_positive)))
            .performClick()

        // The quiz is over for the student once the server has taken the submission
        composeTestRule.waitUntil(DefaultTimeoutMillis) {
            composeTestRule.waitForIdle()
            composeTestRule.onAllNodes(hasTestTag(TEST_TAG_WORK_ON_QUIZ_QUESTIONS_SCREEN)).fetchSemanticsNodes().isEmpty()
        }
    }

    private fun dragOnto(dragItemText: String, dropLocationTag: String) {
        val itemNode = composeTestRule.onNodeWithText(dragItemText)
        val start = itemNode.fetchSemanticsNode().boundsInRoot.center
        val end = composeTestRule.onNodeWithTag(dropLocationTag).fetchSemanticsNode().boundsInRoot.center

        itemNode.performTouchInput {
            swipe(start = center, end = center + (end - start), durationMillis = 800)
        }
        composeTestRule.waitForIdle()
    }

    private fun questionTitle(index: Int, title: String) =
        context.getString(R.string.quiz_participation_question_header_title, index + 1, title)

    private fun submissionOnServer(): QuizSubmission = runBlockingWithTestTimeout {
        val participation = participationService.findParticipation(quiz.id).orThrow("Could not load the participation")
        assertNotNull(participation.quizSubmission, "The server holds no submission")
    }

    /**
     * Which kinds of questions hold an answer that is not empty
     */
    private fun QuizSubmission.answeredKinds(): Set<String> = submittedAnswers.mapNotNull { answer ->
        when (answer) {
            is MultipleChoiceSubmittedAnswer -> "multiple choice".takeIf { answer.selectedOptions.isNotEmpty() }
            is ShortAnswerSubmittedAnswer -> "short answer".takeIf { answer.submittedTexts.any { !it.text.isNullOrBlank() } }
            is DragAndDropSubmittedAnswer -> "drag and drop".takeIf { answer.mappings.isNotEmpty() }
        }
    }.toSet()

    /**
     * The answer to the question of the given type; the quiz has exactly one of each
     */
    private inline fun <reified A : Any> QuizSubmission.answerTo(questionType: String): A {
        val answers = submittedAnswers.filterIsInstance<A>()
        assertEquals(1, answers.size, "Expected one $questionType answer among ${submittedAnswers.map { it::class.simpleName }}")
        return assertIs<A>(answers.single())
    }
}
