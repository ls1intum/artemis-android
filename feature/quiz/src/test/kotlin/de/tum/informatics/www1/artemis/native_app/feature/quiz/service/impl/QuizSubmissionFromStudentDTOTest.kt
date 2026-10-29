package de.tum.informatics.www1.artemis.native_app.feature.quiz.service.impl

import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.JsonProvider
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.DragAndDropQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.MultipleChoiceQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.ShortAnswerQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.QuizSubmission
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.DragAndDropSubmittedAnswer
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.MultipleChoiceSubmittedAnswer
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.ShortAnswerSubmittedAnswer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals

/**
 * Artemis 10 reads the answers of a practice quiz into QuizSubmissionFromStudentDTO, which holds ids only,
 * and answers the entity-shaped submission the live endpoint takes with 400 "Failed to read request".
 */
@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
internal class QuizSubmissionFromStudentDTOTest {

    private val json = JsonProvider().applicationJsonConfiguration

    private val multipleChoiceQuestion = MultipleChoiceQuizQuestion(
        id = 121,
        answerOptions = listOf(
            MultipleChoiceQuizQuestion.AnswerOption(id = 1, text = "correct"),
            MultipleChoiceQuizQuestion.AnswerOption(id = 2, text = "wrong")
        )
    )

    private val shortAnswerQuestion = ShortAnswerQuizQuestion(
        id = 122,
        spots = listOf(
            ShortAnswerQuizQuestion.ShortAnswerSpot(id = 9, spotNr = 1),
            ShortAnswerQuizQuestion.ShortAnswerSpot(id = 10, spotNr = 2)
        )
    )

    private val dragAndDropQuestion = DragAndDropQuizQuestion(id = 123)

    @Test
    fun `sends the answers of a practice quiz as the ids Artemis reads`() {
        val submission = QuizSubmission(
            submitted = true,
            submittedAnswers = listOf(
                MultipleChoiceSubmittedAnswer(
                    quizQuestion = multipleChoiceQuestion,
                    selectedOptions = listOf(multipleChoiceQuestion.answerOptions.first())
                ),
                DragAndDropSubmittedAnswer(
                    quizQuestion = dragAndDropQuestion,
                    mappings = listOf(
                        DragAndDropSubmittedAnswer.DragAndDropMapping(
                            dragItem = DragAndDropQuizQuestion.DragItem(id = 5),
                            dropLocation = DragAndDropQuizQuestion.DropLocation(id = 7)
                        )
                    )
                ),
                ShortAnswerSubmittedAnswer(
                    quizQuestion = shortAnswerQuestion,
                    submittedTexts = listOf(
                        ShortAnswerSubmittedAnswer.ShortAnswerSubmittedText(
                            text = "is",
                            spot = ShortAnswerQuizQuestion.ShortAnswerSpot(id = 9, spotNr = 1)
                        )
                    )
                )
            )
        )

        assertEquals(
            Json.parseToJsonElement(
                """
                {"submittedAnswers":[
                  {"type":"multiple-choice","questionId":121,"selectedOptions":[1]},
                  {"type":"drag-and-drop","questionId":123,"mappings":[{"dragItemId":5,"dropLocationId":7}]},
                  {"type":"short-answer","questionId":122,"submittedTexts":[{"text":"is","spotId":9}]}
                ]}
                """
            ),
            json.encodeToJsonElement(QuizSubmissionFromStudentDTO.of(submission))
        )
    }

    @Test
    fun `leaves out the short answer spots the student left empty`() {
        // Artemis rejects a blank submitted text rather than reading it as unanswered
        val submission = QuizSubmission(
            submittedAnswers = listOf(
                ShortAnswerSubmittedAnswer(
                    quizQuestion = shortAnswerQuestion,
                    submittedTexts = listOf(
                        ShortAnswerSubmittedAnswer.ShortAnswerSubmittedText(
                            text = "input",
                            spot = ShortAnswerQuizQuestion.ShortAnswerSpot(id = 9, spotNr = 1)
                        ),
                        ShortAnswerSubmittedAnswer.ShortAnswerSubmittedText(
                            text = "  ",
                            spot = ShortAnswerQuizQuestion.ShortAnswerSpot(id = 10, spotNr = 2)
                        )
                    )
                )
            )
        )

        assertEquals(
            Json.parseToJsonElement(
                """
                {"submittedAnswers":[
                  {"type":"short-answer","questionId":122,"submittedTexts":[{"text":"input","spotId":9}]}
                ]}
                """
            ),
            json.encodeToJsonElement(QuizSubmissionFromStudentDTO.of(submission))
        )
    }
}
