package de.tum.informatics.www1.artemis.native_app.feature.quiz.training

import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.*
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.*
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.experimental.categories.Category

@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
@Category(UnitTest::class)
class QuizTrainingAccessibilityTest : BaseComposeTest() {
    private fun session(q: QuizQuestion): QuizTrainingSession =
        QuizTrainingSession(
                object : QuizTrainingApi {
                    override suspend fun loadOverview() = TrainingOverview()

                    override suspend fun loadSettings() = TrainingPrivacy(false)

                    override suspend fun updatePrivacy(show: Boolean) = Unit

                    override suspend fun loadPage(request: TrainingPageRequest) =
                        TrainingPage(listOf(TrainingQuestion(q, true, emptyList())), false)

                    override suspend fun submit(
                        question: TrainingQuestion,
                        payload: kotlinx.serialization.json.JsonObject,
                    ): SubmittedAnswer = error("No automatic submission")
                }
            )
            .also { runBlocking { it.start() } }

    @Test
    fun `native short answer field exposes stable localized blank name and course precision`() {
        val q =
            ShortAnswerQuizQuestion(
                id = 7,
                text = "Algorithm: [-spot 1]",
                points = 2.0,
                spots =
                    listOf(
                        ShortAnswerQuizQuestion.ShortAnswerSpot(id = 913, spotNr = 1, width = 18)
                    ),
            )
        val s = session(q)
        composeTestRule.setContent {
            val state by s.state.collectAsState()
            TrainingQuestionContent(state, s, 3)
        }
        composeTestRule.onNodeWithContentDescription("Blank 1").performTextInput("binary search")
        assertEquals("binary search", s.state.value.draft.texts[1])
    }

    @Test
    fun `evaluated question uses originating course precision`() {
        val q =
            ShortAnswerQuizQuestion(
                id = 7,
                text = "Algorithm: [-spot 1]",
                points = 2.0,
                spots =
                    listOf(
                        ShortAnswerQuizQuestion.ShortAnswerSpot(id = 913, spotNr = 1, width = 18)
                    ),
            )
        val s = session(q)
        val evaluation = ShortAnswerSubmittedAnswer(quizQuestion = q, scoreInPoints = 1.234)
        val state = s.state.value.copy(stage = TrainingStage.EVALUATED, evaluation = evaluation)
        composeTestRule.setContent { TrainingQuestionContent(state, s, 3) }
        composeTestRule.onNodeWithText("Your score: 1.234/2").assertExists()
    }

    @Test
    fun `native choice list can assign and clear targets without dragging`() {
        val q =
            DragAndDropQuizQuestion(
                id = 8,
                dragItems =
                    listOf(
                        DragAndDropQuizQuestion.DragItem(id = 501, text = "Alpha"),
                        DragAndDropQuizQuestion.DragItem(id = 502, text = "Beta"),
                    ),
                dropLocations =
                    listOf(
                        DragAndDropQuizQuestion.DropLocation(id = 91, posX = 10.0, posY = 20.0),
                        DragAndDropQuizQuestion.DropLocation(id = 92, posX = 50.0, posY = 20.0),
                    ),
            )
        val s = session(q)
        composeTestRule.setContent {
            val state by s.state.collectAsState()
            TrainingDragAndDropChoices(q, state, s)
        }
        composeTestRule.onNodeWithText("Target 1: Empty").performClick()
        composeTestRule.onNodeWithText("Alpha").performClick()
        assertEquals(mapOf(91L to 501L), s.state.value.draft.mappings)
        composeTestRule.onNodeWithText("Target 2: Empty").performClick()
        composeTestRule.onNodeWithText("Alpha").performClick()
        assertEquals(mapOf(92L to 501L), s.state.value.draft.mappings)
        composeTestRule.onNodeWithText("Target 2: Alpha").performClick()
        composeTestRule.onNodeWithText("Leave empty").performClick()
        assertTrue(s.state.value.draft.mappings.isEmpty())
    }

    @Test
    fun `secondary leaderboard context names the actual league and the students own rank`() {
        composeTestRule.setContent {
            TrainingLeaderboardContext(TrainingEntry(selectedLeague = 5, rank = 7))
        }
        composeTestRule.onNodeWithText("League: Bronze").assertExists()
        composeTestRule.onNodeWithText("Your rank: 7").assertExists()
    }

    @Test
    fun `unknown league is omitted without hiding the students own rank`() {
        composeTestRule.setContent {
            TrainingLeaderboardContext(TrainingEntry(selectedLeague = 999, rank = 7))
        }
        composeTestRule.onNodeWithText("League:", substring = true).assertDoesNotExist()
        composeTestRule.onNodeWithText("Your rank: 7").assertExists()
    }
}
