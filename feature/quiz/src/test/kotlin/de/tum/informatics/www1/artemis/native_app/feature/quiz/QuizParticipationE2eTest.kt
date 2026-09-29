package de.tum.informatics.www1.artemis.native_app.feature.quiz

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.common.test.testServerUrl
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.DragAndDropQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.MultipleChoiceQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.ShortAnswerQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.QuizSubmission
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.DragAndDropSubmittedAnswer
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.MultipleChoiceSubmittedAnswer
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.ShortAnswerSubmittedAnswer
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.endQuizExerciseNow
import de.tum.informatics.www1.artemis.native_app.feature.login.test.getAdminAccessToken
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizParticipationScreen
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizParticipationViewModel
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizQuestionData
import de.tum.informatics.www1.artemis.native_app.feature.quiz.view_result.QuizResultViewModel
import kotlin.test.assertNotNull
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
    fun `shows the saved answers to every kind of question when a live quiz is opened again`() {
        val started = runBlockingWithTestTimeout {
            assertIs<QuizExercise>(participationService.findParticipation(quiz.id).orThrow("Could not load the quiz").exercise)
        }
        val multipleChoice = started.quizQuestions.filterIsInstance<MultipleChoiceQuizQuestion>().single()
        val shortAnswer = started.quizQuestions.filterIsInstance<ShortAnswerQuizQuestion>().single()
        val dragAndDrop = started.quizQuestions.filterIsInstance<DragAndDropQuizQuestion>().single()

        val chosenOption = multipleChoice.answerOptions.first { it.text == "Enter a correct answer option here" }
        val otherOption = multipleChoice.answerOptions.first { it != chosenOption }
        val spot = shortAnswer.spots.first { it.spotNr == 1 }
        val otherSpot = shortAnswer.spots.first { it.spotNr == 2 }
        val placedItem = dragAndDrop.dragItems.first { it.text == "item1" }
        val otherItem = dragAndDrop.dragItems.first { it.text == "item2" }
        val dropLocation = dragAndDrop.dropLocations.first()

        runBlockingWithTestTimeout {
            quizParticipationService
                .saveForLiveMode(
                    QuizSubmission(
                        submitted = false,
                        submissionDate = Clock.System.now(),
                        submittedAnswers = listOf(
                            MultipleChoiceSubmittedAnswer(quizQuestion = multipleChoice, selectedOptions = listOf(chosenOption)),
                            ShortAnswerSubmittedAnswer(
                                quizQuestion = shortAnswer,
                                submittedTexts = listOf(ShortAnswerSubmittedAnswer.ShortAnswerSubmittedText(text = "is", spot = spot))
                            ),
                            DragAndDropSubmittedAnswer(
                                quizQuestion = dragAndDrop,
                                mappings = listOf(DragAndDropSubmittedAnswer.DragAndDropMapping(dragItem = placedItem, dropLocation = dropLocation))
                            )
                        )
                    ),
                    quiz.id,
                    testServerUrl,
                    accessToken
                )
                .orThrow("Could not save the answers")
        }

        val viewModel = setupUi(quiz.id) { viewModel ->
            QuizParticipationScreen(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
                onNavigateToInspectResult = {},
                onNavigateUp = {}
            )
        }

        waitUntilViewModelsSettle { restoredAnswers(viewModel) != null }

        val restored = assertNotNull(restoredAnswers(viewModel))
        assertEquals(mapOf(chosenOption.id to true), restored.multipleChoice.optionSelectionMapping.filterValues { it })
        assertEquals(false, restored.multipleChoice.optionSelectionMapping[otherOption.id] ?: false, "The other option must stay unselected")

        assertEquals("is", restored.shortAnswer.solutionTexts[spot.spotNr])
        assertEquals("", restored.shortAnswer.solutionTexts[otherSpot.spotNr].orEmpty(), "The other spot must stay empty")

        assertEquals(mapOf(dropLocation.id to placedItem.id), restored.dragAndDrop.dropLocationMapping.map { (location, item) -> location.id to item.id }.toMap())
        assertEquals(listOf(otherItem.id), restored.dragAndDrop.availableDragItems.map { it.id }, "Only the item that was not placed is still to be dragged")
    }

    private class RestoredAnswers(
        val multipleChoice: QuizQuestionData.MultipleChoiceData.Editable,
        val shortAnswer: QuizQuestionData.ShortAnswerData.Editable,
        val dragAndDrop: QuizQuestionData.DragAndDropData.Editable
    )

    /**
     * What the questions of the screen hold, once the saved answers have been put into them
     */
    private fun restoredAnswers(viewModel: QuizParticipationViewModel): RestoredAnswers? {
        val questions = (viewModel.quizQuestionsWithData.value as? DataState.Success)?.data.orEmpty()
        val multipleChoice = questions.filterIsInstance<QuizQuestionData.MultipleChoiceData.Editable>().singleOrNull()
        val shortAnswer = questions.filterIsInstance<QuizQuestionData.ShortAnswerData.Editable>().singleOrNull()
        val dragAndDrop = questions.filterIsInstance<QuizQuestionData.DragAndDropData.Editable>().singleOrNull()

        return if (multipleChoice != null && shortAnswer != null && dragAndDrop != null &&
            multipleChoice.optionSelectionMapping.isNotEmpty() && shortAnswer.solutionTexts.isNotEmpty() && dragAndDrop.dropLocationMapping.isNotEmpty()
        ) RestoredAnswers(multipleChoice, shortAnswer, dragAndDrop) else null
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
