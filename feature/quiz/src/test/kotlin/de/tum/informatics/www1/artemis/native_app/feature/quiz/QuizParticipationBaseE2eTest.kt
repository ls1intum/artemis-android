package de.tum.informatics.www1.artemis.native_app.feature.quiz

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import de.tum.informatics.www1.artemis.native_app.core.data.filterSuccess
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.QuizSubmission
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.DragAndDropSubmittedAnswer
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.MultipleChoiceSubmittedAnswer
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.ShortAnswerSubmittedAnswer
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createExercise
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createQuizExercise
import de.tum.informatics.www1.artemis.native_app.core.common.test.testServerUrl
import de.tum.informatics.www1.artemis.native_app.core.ui.common.TEST_TAG_BUTTON_WITH_LOADING_ANIMATION_LOADING
import de.tum.informatics.www1.artemis.native_app.feature.login.test.getAdminAccessToken
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizParticipationScreen
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizParticipationViewModel
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizQuestionData
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work.TEST_TAG_WORK_ON_QUIZ_QUESTIONS_SCREEN
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.robolectric.util.Logger
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
internal abstract class QuizParticipationBaseE2eTest(quizType: QuizType.WorkableQuizType) :
    QuizBaseE2eTest(quizType) {

    protected lateinit var quiz: QuizExercise

    override suspend fun setupHook() {
        super.setupHook()

        val path = uploadBackgroundImage()

        quiz = assertIs(
            createExercise(
                accessToken = getAdminAccessToken(),
                courseId = courseId,
                endpoint = "quiz-exercises",
                creator = { name, courseId ->
                    createQuizExercise(name, courseId, path)
                }
            )
        )
    }

    protected fun testSubmitDragAndDropImpl() {
        testQuizSubmissionImpl(
            setupAndVerify = { viewModel, submit ->
                val quizQuestionsWithData = viewModel.quizQuestionsWithData.filterSuccess().first()
                val dndData =
                    quizQuestionsWithData.filterIsInstance<QuizQuestionData.DragAndDropData.Editable>()
                        .first()

                assertTrue(
                    dndData.availableDragItems.isNotEmpty(),
                    "No drag items are available. The test makes no sense."
                )
                assertTrue(
                    dndData.question.dropLocations.isNotEmpty(),
                    "No drop locations are present. The test makes no sense."
                )

                val selectedItem = dndData.availableDragItems.first()
                val selectedDropLocation = dndData.question.dropLocations.first()

                dndData.onDragItemIntoDropLocation(selectedItem.id, selectedDropLocation.id)

                val submission = submit()

                val submittedAnswer = submission.submittedAnswers
                    .filterIsInstance<DragAndDropSubmittedAnswer>()
                    .firstOrNull { it.quizQuestion?.id == dndData.question.id }
                    ?: throw NoSuchElementException("Could not find data for selected question with id ${dndData.question.id} in quiz questions ${submission.submittedAnswers}")

                assertTrue(
                    submittedAnswer
                        .mappings
                        .any { it.dragItem?.id == selectedItem.id && it.dropLocation?.id == selectedDropLocation.id },
                    "Downloaded submitted answers $submittedAnswer do not contain expected mapping with dragItemId=${selectedItem.id} and dropLocationId=${selectedDropLocation.id}"
                )
            }
        )
    }

    protected fun testSubmitShortAnswerImpl() {
        testQuizSubmissionImpl(
            setupAndVerify = { viewModel, submit ->
                val quizQuestionsWithData = viewModel.quizQuestionsWithData.filterSuccess().first()
                val saData =
                    quizQuestionsWithData.filterIsInstance<QuizQuestionData.ShortAnswerData.Editable>()
                        .first()

                assertTrue(
                    saData.question.spots.isNotEmpty(),
                    "No spots in this question, which makes the whole test useless."
                )

                val selectedSpot = saData.question.spots.first()

                val spotNr = assertNotNull(
                    selectedSpot.spotNr,
                    "spotNr is null on spot=$selectedSpot in question=${saData.question}"
                )

                val enteredText = "Answer $spotNr"
                saData.onUpdateSolutionText(spotNr, enteredText)

                val submission = submit()
                val submittedAnswer = submission.submittedAnswers
                    .filterIsInstance<ShortAnswerSubmittedAnswer>()
                    .firstOrNull { it.quizQuestion?.id == saData.question.id }
                    ?: throw NoSuchElementException("Could not find data for selected question with id ${saData.question.id} in quiz questions ${submission.submittedAnswers}")

                assertEquals(
                    1,
                    submittedAnswer.submittedTexts.size,
                    "Number of spots does not match submitted texts"
                )
                val submittedText = submittedAnswer.submittedTexts.first()
                assertEquals(
                    enteredText,
                    submittedText.text,
                    "The text we've entered does not match the text the server provided"
                )
            }
        )
    }

    protected fun testSubmitMultipleChoiceImpl() {
        testQuizSubmissionImpl(
            setupAndVerify = { viewModel, submit ->
                val quizQuestionsWithData = viewModel.quizQuestionsWithData.filterSuccess().first()
                val mcData =
                    quizQuestionsWithData.filterIsInstance<QuizQuestionData.MultipleChoiceData.Editable>()
                        .first()

                val selectedOption = mcData.question.answerOptions.first()

                mcData.onRequestChangeAnswerOptionSelectionState(selectedOption.id, true)

                val submission = submit()

                val submittedAnswer =
                    submission.submittedAnswers
                        .filterIsInstance<MultipleChoiceSubmittedAnswer>()
                        .firstOrNull { it.quizQuestion?.id == mcData.question.id }
                        ?: throw NoSuchElementException("Could not find data for selected question with id ${mcData.question.id} in quiz questions ${submission.submittedAnswers}")

                assertEquals(
                    1,
                    submittedAnswer.selectedOptions.size,
                    "We only checked one option. So we only expect one selected option."
                )
                assertEquals(
                    selectedOption.id,
                    submittedAnswer.selectedOptions.first().id,
                    "Selected option does not match the option we selected."
                )
            }
        )
    }

    protected fun testQuizSubmissionImpl(
        setupAndVerify: suspend (QuizParticipationViewModel, submit: () -> QuizSubmission) -> Unit
    ) {
        val viewModel = setupUi(quiz.id) { viewModel ->
            QuizParticipationScreen(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
                onNavigateToInspectResult = {},
                onNavigateUp = {}
            )
        }

        runBlockingWithTestTimeout {
            setupAndVerify(viewModel) {
                // Lambda to submit

                composeTestRule.waitUntilExactlyOneExists(
                    hasTestTag(TEST_TAG_WORK_ON_QUIZ_QUESTIONS_SCREEN),
                    DefaultTimeoutMillis
                )

                composeTestRule
                    .onNodeWithText(context.getString(R.string.quiz_participation_submit_button))
                    .performClick()

                composeTestRule
                    .onNode(hasAnyAncestor(isDialog()) and hasText(context.getString(R.string.quiz_participation_submit_dialog_positive)))
                    .performClick()

                // Wait until loading is complete
                composeTestRule
                    .waitUntilDoesNotExist(
                        hasTestTag(TEST_TAG_BUTTON_WITH_LOADING_ANIMATION_LOADING),
                        DefaultTimeoutMillis
                    )

                val result = runBlockingWithTestTimeout {
                    when (quizType) {
                        QuizType.Live -> {
                            val participation = participationService
                                .findParticipation(quiz.id, testServerUrl, accessToken)
                                .orThrow("Could not load submitted participation")

                            val results =
                                assertNotNull(
                                    participation.results,
                                    "Results is null on participation"
                                )
                            assertTrue(
                                results.isNotEmpty(),
                                "Results does not contain any element"
                            )
                            results.first()
                        }

                        QuizType.Practice -> {
                            viewModel.result.first()
                        }
                    }
                }

                assertNotNull(result, "Result is null")

                val submission: QuizSubmission =
                    assertIs(
                        result.submission,
                        "Submission in result is not a QuizSubmission"
                    )

                Logger.info("Loaded submitted submission: $submission")

                submission
            }
        }
    }
}