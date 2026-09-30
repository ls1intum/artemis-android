package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.participate.quiz

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createQuiz
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.endQuizExerciseNow
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.joinQuiz
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.submitLiveQuiz
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.participate.text_exercise.BaseExerciseTest
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.ExerciseViewModel
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.home.ExerciseScreen
import de.tum.informatics.www1.artemis.native_app.feature.login.test.getAdminAccessToken
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.ParticipationService
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.test.get
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import de.tum.informatics.www1.artemis.native_app.core.ui.R as CoreUiR

/**
 * The exercise screen of a quiz, as Artemis describes the quiz to a student: which buttons lead into the quiz,
 * to practising it and to its result, and where they lead.
 */
@OptIn(ExperimentalTestApi::class)
@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
class QuizExerciseOverviewE2eTest : BaseExerciseTest() {

    private val opened = mutableListOf<Pair<Long, Boolean>>()
    private val results = mutableListOf<Long>()

    private val openQuizText get() = context.getString(CoreUiR.string.exercise_actions_open_quiz_button)
    private val practiceText get() = context.getString(CoreUiR.string.exercise_actions_practice_quiz_button)
    private val viewResultText get() = context.getString(CoreUiR.string.exercise_actions_view_result_button)

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `offers to open a quiz that is running, and does not offer to practise it`() {
        val quiz = createQuizForStudent()

        showExerciseScreen(quiz)

        composeTestRule.waitUntilExactlyOneExists(hasText(openQuizText), DefaultTimeoutMillis)
        composeTestRule.onNodeWithText(practiceText).assertDoesNotExist()
        composeTestRule.onNodeWithText(viewResultText).assertDoesNotExist()

        composeTestRule.onNodeWithText(openQuizText).performClick()

        assertEquals(listOf(course.id!! to false), opened)
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `offers to open a quiz again that the student has started`() {
        val quiz = createQuizForStudent()
        runBlockingWithTestTimeout {
            get<ParticipationService>().findParticipation(quiz.id).orThrow("Could not start the participation")
        }

        showExerciseScreen(quiz)

        composeTestRule.waitUntilExactlyOneExists(hasText(openQuizText), DefaultTimeoutMillis)
        composeTestRule.onNodeWithText(openQuizText).performClick()

        assertEquals(listOf(course.id!! to false), opened)
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `offers to practise a quiz that has ended, and not to open it`() {
        val quiz = createQuizForStudent()
        runBlockingWithTestTimeout { endQuizExerciseNow(getAdminAccessToken(), quiz.id) }

        showExerciseScreen(quiz)

        composeTestRule.waitUntilExactlyOneExists(hasText(practiceText), DefaultTimeoutMillis)
        composeTestRule.onNodeWithText(openQuizText).assertDoesNotExist()

        composeTestRule.onNodeWithText(practiceText).performClick()

        assertEquals(listOf(course.id!! to true), opened)
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `offers the result of a quiz that was submitted and has ended`() {
        val quiz = createQuizForStudent()
        runBlockingWithTestTimeout {
            get<ParticipationService>().findParticipation(quiz.id).orThrow("Could not start the participation")
            joinQuiz(accessToken, quiz.id)
            submitLiveQuiz(accessToken, quiz.id)
            endQuizExerciseNow(getAdminAccessToken(), quiz.id)
        }

        showExerciseScreen(quiz)

        composeTestRule.waitUntilExactlyOneExists(hasText(viewResultText), DefaultTimeoutMillis)
        composeTestRule.onNodeWithText(openQuizText).assertDoesNotExist()

        composeTestRule.onNodeWithText(viewResultText).performClick()

        assertEquals(listOf(course.id!!), results)
    }

    private fun createQuizForStudent(): QuizExercise = runBlockingWithTestTimeout {
        createQuiz(getAdminAccessToken(), course.id!!)
    }

    private fun showExerciseScreen(quiz: QuizExercise) {
        val viewModel = ExerciseViewModel(
            exerciseId = quiz.id,
            exerciseService = get(),
            channelService = get(),
            liveParticipationService = get(),
            courseExerciseService = get(),
            networkStatusProvider = get(),
            coroutineContext = testDispatcher
        )

        composeTestRule.setContent {
            ExerciseScreen(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
                onViewResult = { },
                onViewTextExerciseParticipationScreen = { },
                onParticipateInQuiz = { courseId, isPractice -> opened += courseId to isPractice },
                onClickViewQuizResults = { results += it }
            )
        }
    }
}
