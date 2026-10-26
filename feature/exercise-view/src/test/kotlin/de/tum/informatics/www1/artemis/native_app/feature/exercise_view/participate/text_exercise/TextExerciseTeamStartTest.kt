package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.participate.text_exercise

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.TextExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.StudentParticipation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.TextSubmission
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ResultTemplateStatus
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseActionButtons
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.ExerciseActions
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class TextExerciseTeamStartTest {
    @get:Rule val compose = createComposeRule()
    private var starts = 0
    private var opened: Long? = null
    private fun show(exercise: TextExercise, status: ResultTemplateStatus? = null) {
        compose.setContent {
            MaterialTheme {
                Row {
                ExerciseActionButtons(
                    modifier = Modifier,
                    exercise = exercise,
                    templateStatus = status,
                    actions = ExerciseActions(
                        onClickStartTextExercise = { starts++ },
                        onClickPracticeQuiz = {}, onClickOpenQuiz = {}, onClickStartQuiz = {},
                        onClickOpenTextExercise = { opened = it }, onClickViewResult = {}, onClickViewQuizResults = {}
                    )
                )
                }
            }
        }
        compose.waitForIdle()
    }

    @Test fun `assigned team text exercise can start through the native action`() {
        show(TextExercise(mode = Exercise.Mode.TEAM, teamMode = true, studentAssignedTeamId = 1, studentAssignedTeamIdComputed = true))
        compose.onNodeWithText("Start exercise").assertIsEnabled().performClick()
        assertEquals(1, starts)
    }

    @Test fun `team text start stays disabled without a verified assignment`() {
        show(TextExercise(mode = Exercise.Mode.TEAM, teamMode = true, studentAssignedTeamId = 1, studentAssignedTeamIdComputed = false))
        compose.onNodeWithText("Start exercise").assertIsNotEnabled()
    }

    @Test fun `team text start stays disabled when the student has no assigned team`() {
        show(TextExercise(mode = Exercise.Mode.TEAM, teamMode = true, studentAssignedTeamIdComputed = true))
        compose.onNodeWithText("Start exercise").assertIsNotEnabled()
    }

    @Test fun `assessed text answer can still open the editor and view its result`() {
        val result = Result(id = 7)
        show(TextExercise(studentParticipations = listOf(StudentParticipation.StudentParticipationImpl(
            id = 17, testRun = false, initializationState = Participation.InitializationState.FINISHED,
            submissions = listOf(TextSubmission(id = 3, submitted = true, text = "Assessed answer", results = listOf(result)))
        ))), ResultTemplateStatus.HasResult(result))
        compose.onNodeWithText("View submission").assertIsEnabled().performClick()
        assertEquals(17L, opened)
        compose.onNodeWithText("View result").assertExists()
    }

    @Test fun `individual text exercise retains its start action`() {
        show(TextExercise())
        compose.onNodeWithText("Start exercise").assertIsEnabled().performClick()
        assertEquals(1, starts)
    }
}
