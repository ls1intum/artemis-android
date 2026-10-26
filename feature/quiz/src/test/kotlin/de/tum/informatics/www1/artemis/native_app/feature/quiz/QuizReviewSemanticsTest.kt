package de.tum.informatics.www1.artemis.native_app.feature.quiz

import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.*
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.*
import de.tum.informatics.www1.artemis.native_app.feature.quiz.view_result.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.experimental.categories.Category
import java.util.Locale

@Category(UnitTest::class)
class QuizReviewSemanticsTest {
    private val l1 = DragAndDropQuizQuestion.DropLocation(id = 1)
    private val l2 = DragAndDropQuizQuestion.DropLocation(id = 2)
    private val a = DragAndDropQuizQuestion.DragItem(id = 10)
    private val b = DragAndDropQuizQuestion.DragItem(id = 20)
    private fun pair(location: DragAndDropQuizQuestion.DropLocation, item: DragAndDropQuizQuestion.DragItem) = DragAndDropSubmittedAnswer.DragAndDropMapping(dropLocation = location, dragItem = item)
    private fun question() = DragAndDropQuizQuestion(dropLocations = listOf(l1, l2), dragItems = listOf(a, b), correctMappings = listOf(pair(l1,a), pair(l1,b), pair(l2,a), pair(l2,b)))

    @Test fun `every authoritative DnD alternative is accepted and sample never reuses an item`() {
        val question = question()
        val outcomes = dragAndDropReviewOutcomes(question, mapOf(l1 to a, l2 to b))
        assertEquals(QuizReviewOutcome.CORRECT, outcomes[1])
        assertEquals(QuizReviewOutcome.CORRECT, outcomes[2])
        val sample = dragAndDropReviewSample(question)!!
        assertEquals(2, sample.size)
        assertEquals(setOf(10L,20L), sample.values.map { it.id }.toSet())
        assertTrue(sample.all { (location,item) -> question.correctMappings!!.any { it.dropLocation?.id == location.id && it.dragItem?.id == item.id } })
    }
    @Test fun `DnD withheld invalid parts and unsolvable assignments remain distinct`() {
        assertEquals(QuizReviewOutcome.WITHHELD, dragAndDropReviewOutcomes(question().copy(correctMappings = null), mapOf(l1 to a))[1])
        assertNull(dragAndDropReviewSample(question().copy(correctMappings = listOf(pair(l1,a), pair(l2,a)))))
        assertEquals(QuizReviewOutcome.INVALID, dragAndDropReviewOutcomes(question().copy(invalid = true), mapOf(l1 to a))[1])
        assertEquals(QuizReviewOutcome.INVALID, dragAndDropReviewOutcomes(question().copy(dropLocations = listOf(l1.copy(invalid=true), l2)), mapOf(l1 to a))[1])
        assertEquals(QuizReviewOutcome.INCORRECT, dragAndDropReviewOutcomes(question(), emptyMap())[1])
    }
    @Test fun `short answer review preserves authoritative nullable outcomes by spot number`() {
        val answer = ShortAnswerSubmittedAnswer(submittedTexts = listOf(
            ShortAnswerSubmittedAnswer.ShortAnswerSubmittedText(text="Accepted",isCorrect=true,spot=ShortAnswerQuizQuestion.ShortAnswerSpot(spotNr=1)),
            ShortAnswerSubmittedAnswer.ShortAnswerSubmittedText(text="Rejected",isCorrect=false,spot=ShortAnswerQuizQuestion.ShortAnswerSpot(spotNr=2)),
            ShortAnswerSubmittedAnswer.ShortAnswerSubmittedText(text="Withheld",spot=ShortAnswerQuizQuestion.ShortAnswerSpot(spotNr=3))))
        assertEquals(mapOf(1 to true,2 to false,3 to null), shortAnswerReviewCorrectness(answer))
        assertEquals(QuizReviewOutcome.INVALID, shortAnswerReviewOutcome(ShortAnswerQuizQuestion(invalid=true), ShortAnswerQuizQuestion.ShortAnswerSpot(spotNr=1), true))
        assertEquals(QuizReviewOutcome.WITHHELD, shortAnswerReviewOutcome(ShortAnswerQuizQuestion(), ShortAnswerQuizQuestion.ShortAnswerSpot(spotNr=3), null))
    }
    @Test fun `invalid multiple choice content overrides old correctness`() {
        assertEquals(QuizReviewOutcome.INVALID, multipleChoiceReviewOutcome(MultipleChoiceQuizQuestion(invalid=true), MultipleChoiceQuizQuestion.AnswerOption(isCorrect=false)))
        assertEquals(QuizReviewOutcome.INVALID, multipleChoiceReviewOutcome(MultipleChoiceQuizQuestion(), MultipleChoiceQuizQuestion.AnswerOption(isCorrect=true,invalid=true)))
        assertEquals(QuizReviewOutcome.WITHHELD, multipleChoiceReviewOutcome(MultipleChoiceQuizQuestion(), MultipleChoiceQuizQuestion.AnswerOption()))
    }
    @Test fun `absolute quiz result numbers follow course precision and preserve unknown and zero`() {
        val formatted = quizReviewScore(1.25, 3.0, 1, Locale.US)
        assertEquals("1.3", formatted.points)
        assertEquals("3", formatted.maximum)
        assertEquals("41.7%", formatted.percentage)
        assertEquals("0", quizReviewScore(0.0, 3.0, 1, Locale.US).points)
        assertNull(quizReviewScore(null, 3.0, 1, Locale.US).points)
        assertNull(quizReviewScore(1.0, 0.0, 1, Locale.US).percentage)
    }
    @Test fun `published maximum is authoritative and invalidated questions do not add possible points`() {
        val questions = listOf(MultipleChoiceQuizQuestion(points = 5.0), MultipleChoiceQuizQuestion(points = 10.0, invalid = true))
        assertEquals(8.0, quizReviewMaximum(de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise(maxPoints = 8f, quizQuestions = questions)), 0.0)
        assertEquals(5.0, quizReviewMaximum(de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise(quizQuestions = questions)), 0.0)
    }

}
