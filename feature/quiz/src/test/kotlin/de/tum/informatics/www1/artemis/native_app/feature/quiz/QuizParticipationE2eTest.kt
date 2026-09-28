package de.tum.informatics.www1.artemis.native_app.feature.quiz

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.common.test.testServerUrl
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.MultipleChoiceQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.QuizSubmission
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.MultipleChoiceSubmittedAnswer
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.endQuizExerciseNow
import de.tum.informatics.www1.artemis.native_app.feature.login.test.getAdminAccessToken
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizParticipationScreen
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizQuestionData
import de.tum.informatics.www1.artemis.native_app.feature.quiz.view_result.QuizResultViewModel
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.test.get
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Clock

@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
internal class QuizParticipationE2eTest : QuizParticipationBaseE2eTest(QuizType.Live) {

    override suspend fun setupHook() {
        super.setupHook()

        // Like the app, start the participation before joining: the server keeps the batch the student
        // joins on the submission that starting the participation creates.
        participationService
            .findParticipation(quiz.id)
            .orThrow("Could not start quiz participation")

        quizExerciseService
            .join(quiz.id, "", testServerUrl, accessToken)
            .orThrow("Could not join the quiz")
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can submit live quiz - empty submission`() {
        testQuizSubmissionImpl(
            setupAndVerify = { _, submit -> submit() }
        )
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can submit live quiz - multiple choice`() {
        testSubmitMultipleChoiceImpl()
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can submit live quiz - short answer`() {
        testSubmitShortAnswerImpl()
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can submit live quiz - drag and drop`() {
        testSubmitDragAndDropImpl()
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `shows the saved answers when a live quiz is opened again`() {
        val (question, savedOption) = runBlockingWithTestTimeout {
            val (question, option) = loadFirstMultipleChoiceOption()

            quizParticipationService
                .saveForLiveMode(
                    submissionSelecting(question, option, submitted = false),
                    quiz.id,
                    testServerUrl,
                    accessToken
                )
                .orThrow("Could not save the answers")

            question to option
        }

        val viewModel = setupUi(quiz.id) { viewModel ->
            QuizParticipationScreen(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
                onNavigateToInspectResult = {},
                onNavigateUp = {}
            )
        }

        waitUntilViewModelsSettle {
            val questions = (viewModel.quizQuestionsWithData.value as? DataState.Success)?.data.orEmpty()

            questions
                .filterIsInstance<QuizQuestionData.MultipleChoiceData.Editable>()
                .any { it.question.id == question.id && it.optionSelectionMapping[savedOption.id] == true }
        }
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `shows the result once a live quiz has ended`() {
        val (question, submittedOption) = runBlockingWithTestTimeout {
            val (question, option) = loadFirstMultipleChoiceOption()

            quizParticipationService
                .submitForLiveMode(
                    submissionSelecting(question, option, submitted = true),
                    quiz.id,
                    testServerUrl,
                    accessToken
                )
                .orThrow("Could not submit the answers")

            endQuizExerciseNow(getAdminAccessToken(), quiz.id)

            question to option
        }

        var inspectedResult: QuizType.ViewableQuizType? = null

        setupUi(quiz.id) { viewModel ->
            QuizParticipationScreen(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
                onNavigateToInspectResult = { inspectedResult = it },
                onNavigateUp = {}
            )
        }

        val resultViewModel = QuizResultViewModel(
            exerciseId = quiz.id,
            quizType = QuizType.ViewResults,
            quizExerciseService = get(),
            networkStatusProvider = get(),
            serverConfigurationService = get(),
            accountService = get(),
            participationService = get()
        )

        // The participation screen moves on to the result screen as soon as it has the result
        waitUntilViewModelsSettle { inspectedResult == QuizType.ViewResults }

        waitUntilViewModelsSettle {
            resultViewModel.result.value !is DataState.Loading && resultViewModel.submission.value !is DataState.Loading
        }

        assertIs<DataState.Success<*>>(
            resultViewModel.result.value,
            "The result screen could not load the result"
        )

        val submittedAnswer = assertIs<DataState.Success<QuizSubmission>>(
            resultViewModel.submission.value,
            "The result screen could not load the submission"
        )
            .data
            .submittedAnswers
            .filterIsInstance<MultipleChoiceSubmittedAnswer>()
            .first { it.quizQuestion?.id == question.id }

        assertEquals(
            listOf(submittedOption.id),
            submittedAnswer.selectedOptions.map { it.id },
            "The result shows other answers than the ones submitted"
        )
    }

    /**
     * The view models carry on through the main looper. Waiting for a node runs what is queued there,
     * waitUntil alone does not, so a view model created after setContent would never get anywhere.
     */
    private fun waitUntilViewModelsSettle(condition: () -> Boolean) {
        composeTestRule.waitUntil(DefaultTimeoutMillis) {
            composeTestRule.waitForIdle()
            condition()
        }
    }

    /**
     * Only the participation of a quiz the student has started carries its questions
     */
    private suspend fun loadFirstMultipleChoiceOption(): Pair<MultipleChoiceQuizQuestion, MultipleChoiceQuizQuestion.AnswerOption> {
        val startedQuiz = assertIs<QuizExercise>(
            participationService
                .findParticipation(quiz.id)
                .orThrow("Could not load the participation")
                .exercise
        )

        val question = startedQuiz.quizQuestions.filterIsInstance<MultipleChoiceQuizQuestion>().first()
        return question to question.answerOptions.first()
    }

    private fun submissionSelecting(
        question: MultipleChoiceQuizQuestion,
        option: MultipleChoiceQuizQuestion.AnswerOption,
        submitted: Boolean
    ) = QuizSubmission(
        submitted = submitted,
        submissionDate = Clock.System.now(),
        submittedAnswers = listOf(
            MultipleChoiceSubmittedAnswer(quizQuestion = question, selectedOptions = listOf(option))
        )
    )
}
