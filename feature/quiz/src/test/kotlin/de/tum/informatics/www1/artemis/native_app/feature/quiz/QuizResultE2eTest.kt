package de.tum.informatics.www1.artemis.native_app.feature.quiz

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.common.test.testServerUrl
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.MultipleChoiceQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.ShortAnswerQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.QuizSubmission
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.MultipleChoiceSubmittedAnswer
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.ShortAnswerSubmittedAnswer
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createQuiz
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.endQuizExerciseNow
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.LocalArtemisImageProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.impl.ArtemisImageProviderImpl
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.impl.BaseImageProviderImpl
import de.tum.informatics.www1.artemis.native_app.feature.login.test.getAdminAccessToken
import de.tum.informatics.www1.artemis.native_app.feature.quiz.view_result.ViewQuizResultScreen
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.test.get
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals
import kotlin.time.Clock

/**
 * The screen a student sees for the result of a quiz. The student answered the multiple choice question
 * right, one of the two spots of the short answer question right and the drag and drop question not at all,
 * so the points of the questions are 1, 0.5 and 0 of 1.
 */
@OptIn(ExperimentalTestApi::class)
@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
// The drag and drop question shows its background image, which needs the native graphics of Robolectric to be decoded
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h1200dp")
internal class QuizResultE2eTest : QuizBaseE2eTest(QuizType.Live) {

    private lateinit var quiz: QuizExercise

    override suspend fun setupHook() {
        super.setupHook()

        quiz = createQuiz(getAdminAccessToken(), courseId, randomizeQuestionOrder = false)

        // Like the app, start the participation before joining; the questions come once the batch has started
        participationService.findParticipation(quiz.id).orThrow("Could not start the quiz")
        quizExerciseService.join(quiz.id, "", testServerUrl, accessToken).orThrow("Could not join the quiz")
        val started = assertIsQuiz(participationService.findParticipation(quiz.id).orThrow("Could not load the quiz").exercise)

        val multipleChoice = started.quizQuestions.filterIsInstance<MultipleChoiceQuizQuestion>().single()
        val shortAnswer = started.quizQuestions.filterIsInstance<ShortAnswerQuizQuestion>().single()

        quizParticipationService.submitForLiveMode(
            QuizSubmission(
                submitted = true,
                submissionDate = Clock.System.now(),
                submittedAnswers = listOf(
                    MultipleChoiceSubmittedAnswer(
                        quizQuestion = multipleChoice,
                        selectedOptions = listOf(multipleChoice.answerOptions.first { it.text == "Enter a correct answer option here" })
                    ),
                    ShortAnswerSubmittedAnswer(
                        quizQuestion = shortAnswer,
                        submittedTexts = listOf(
                            ShortAnswerSubmittedAnswer.ShortAnswerSubmittedText(
                                text = "is",
                                spot = shortAnswer.spots.first { it.spotNr == 1 }
                            )
                        )
                    )
                )
            ),
            quiz.id, testServerUrl, accessToken
        ).orThrow("Could not submit the quiz")

        endQuizExerciseNow(getAdminAccessToken(), quiz.id)
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `shows the points of the whole quiz and of each question`() {
        showResult()

        composeTestRule.onNodeWithText("You completed the quiz with 1.5 out of 3 points (50%).").assertExists()

        // Each of the three questions shows its own points, and no two are alike
        composeTestRule.onNodeWithText("Your score: 1/1").assertExists()
        composeTestRule.onNodeWithText("Your score: 0.5/1").assertExists()
        composeTestRule.onNodeWithText("Your score: 0/1").assertExists()
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `marks the options of a multiple choice question as correct or wrong`() {
        showResult()

        composeTestRule.onAllNodesWithText("Correct").fetchSemanticsNodes().let {
            assertEquals(1, it.size, "One option is correct")
        }
        composeTestRule.onAllNodesWithText("Wrong").fetchSemanticsNodes().let {
            assertEquals(1, it.size, "One option is wrong")
        }
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `shows the sample solutions of the short answer and the drag and drop question on request`() {
        showResult()

        // The short answer question comes first, the drag and drop question last
        composeTestRule.onAllNodesWithText("Show sample solution").assertCountEquals(2)
        composeTestRule.onAllNodesWithText("Show sample solution")[0].performClick()
        composeTestRule.onAllNodesWithText("Hide sample solution").assertCountEquals(1)
        composeTestRule.onAllNodesWithText("Show sample solution").assertCountEquals(1)

        // The result is a long list, the button of the last question is beyond its end
        composeTestRule.onAllNodes(hasScrollToNodeAction()).onLast().performScrollToNode(hasText("Show sample solution"))
        composeTestRule.onAllNodesWithText("Show sample solution")[0].performClick()
        composeTestRule.onAllNodesWithText("Hide sample solution").assertCountEquals(2)
        composeTestRule.onAllNodesWithText("Show sample solution").assertCountEquals(0)
    }

    private fun showResult() {
        val imageProvider = ArtemisImageProviderImpl(get<ArtemisContextProvider>(), BaseImageProviderImpl())

        composeTestRule.setContent {
            CompositionLocalProvider(LocalArtemisImageProvider provides imageProvider) {
                ViewQuizResultScreen(
                    modifier = Modifier.fillMaxSize(),
                    exerciseId = quiz.id,
                    quizType = QuizType.ViewResults
                )
            }
        }

        composeTestRule.waitUntilAtLeastOneExists(hasText("You completed the quiz", substring = true), DefaultTimeoutMillis)
    }

    private fun assertIsQuiz(exercise: Any?): QuizExercise = exercise as? QuizExercise ?: error("Not a quiz: $exercise")
}
