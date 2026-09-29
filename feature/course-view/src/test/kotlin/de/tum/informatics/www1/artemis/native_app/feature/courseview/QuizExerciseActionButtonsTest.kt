package de.tum.informatics.www1.artemis.native_app.feature.courseview

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.StudentParticipation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import de.tum.informatics.www1.artemis.native_app.core.ui.R
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseActionButtons
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseActions
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ResultTemplateStatus
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.time.Instant

/**
 * The buttons through which a student takes part in a live quiz from the exercise list and the exercise
 * screen. The exercises carry what the course dashboard of Artemis 10 sends for a quiz: no batches and no
 * quiz mode, only the participation of the student, if any.
 */
@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class QuizExerciseActionButtonsTest : BaseComposeTest() {

    private val clickedActions = mutableListOf<String>()

    private val actions = ExerciseActions(
        onClickStartTextExercise = { clickedActions += "start text exercise" },
        onClickPracticeQuiz = { clickedActions += "practice quiz" },
        onClickOpenQuiz = { clickedActions += "open quiz" },
        onClickStartQuiz = { clickedActions += "start quiz" },
        onClickOpenTextExercise = { clickedActions += "open text exercise" },
        onClickViewResult = { clickedActions += "view result" },
        onClickViewQuizResults = { clickedActions += "view quiz results" }
    )

    private val openQuizText get() = context.getString(R.string.exercise_actions_open_quiz_button)
    private val startQuizText get() = context.getString(R.string.exercise_actions_start_quiz_button)
    private val viewResultText get() = context.getString(R.string.exercise_actions_view_result_button)
    private val practiceText get() = context.getString(R.string.exercise_actions_practice_quiz_button)

    @Test
    fun `offers to open a quiz the student has not joined yet`() {
        setupUi(QuizExercise(id = 1))

        composeTestRule.onNodeWithText(openQuizText).performClick()

        assertEquals(listOf("open quiz"), clickedActions)
    }

    @Test
    fun `offers to start a quiz whose batch has already started`() {
        setupUi(
            QuizExercise(
                id = 1,
                quizBatches = listOf(QuizExercise.QuizBatch(id = 1, started = true, ended = false))
            )
        )

        composeTestRule.onNodeWithText(startQuizText).performClick()

        assertEquals(listOf("start quiz"), clickedActions)
    }

    @Test
    fun `offers to open a quiz again that the student takes part in`() {
        setupUi(
            QuizExercise(
                id = 1,
                studentParticipations = listOf(participation(Participation.InitializationState.INITIALIZED))
            )
        )

        composeTestRule.onNodeWithText(openQuizText).performClick()

        assertEquals(listOf("open quiz"), clickedActions)
    }

    @Test
    fun `offers no way back into a quiz the student has submitted`() {
        setupUi(
            QuizExercise(
                id = 1,
                studentParticipations = listOf(participation(Participation.InitializationState.FINISHED))
            )
        )

        composeTestRule.onNodeWithText(openQuizText).assertDoesNotExist()
        composeTestRule.onNodeWithText(startQuizText).assertDoesNotExist()
    }

    @Test
    fun `shows the result of a quiz on the quiz result screen`() {
        setupUi(
            QuizExercise(
                id = 1,
                studentParticipations = listOf(participation(Participation.InitializationState.FINISHED))
            ),
            templateStatus = ResultTemplateStatus.HasResult(
                Result(
                    id = 2,
                    completionDate = Instant.parse("2026-09-28T20:29:41.141Z"),
                    successful = false,
                    score = 33.3f,
                    rated = true,
                    assessmentType = Exercise.AssessmentType.AUTOMATIC
                )
            )
        )

        composeTestRule.onNodeWithText(viewResultText).performClick()

        assertEquals(listOf("view quiz results"), clickedActions)
    }

    @Test
    fun `offers to practise a quiz that has ended`() {
        // Artemis opens every course quiz for practice once it has ended, and no longer sends a flag for it
        setupUi(
            QuizExercise(
                id = 1,
                dueDate = Instant.parse("2026-09-28T20:29:41Z"),
                studentParticipations = listOf(participation(Participation.InitializationState.FINISHED))
            )
        )

        composeTestRule.onNodeWithText(practiceText).performClick()

        assertEquals(listOf("practice quiz"), clickedActions)
    }

    private fun participation(state: Participation.InitializationState) =
        StudentParticipation.StudentParticipationImpl(
            id = 1,
            initializationState = state,
            initializationDate = Instant.parse("2026-09-28T20:29:10.373Z"),
            testRun = false
        )

    private fun setupUi(exercise: QuizExercise, templateStatus: ResultTemplateStatus? = null) {
        composeTestRule.setContent {
            ExerciseActionButtons(
                modifier = Modifier,
                exercise = exercise,
                templateStatus = templateStatus,
                actions = actions
            )
        }
    }
}
