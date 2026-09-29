package de.tum.informatics.www1.artemis.native_app.feature.quiz.service.impl

import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.QuizSubmission
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.DragAndDropSubmittedAnswer
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.MultipleChoiceSubmittedAnswer
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.ShortAnswerSubmittedAnswer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The answers of a practice quiz as Artemis reads them: by the ids of the questions and of what was chosen,
 * like the web app sends them. The live endpoint still takes the whole submission.
 */
@Serializable
internal data class QuizSubmissionFromStudentDTO(val submittedAnswers: List<SubmittedAnswer>) {

    @Serializable
    sealed interface SubmittedAnswer {

        @Serializable
        @SerialName("multiple-choice")
        data class MultipleChoice(val questionId: Long, val selectedOptions: List<Long>) : SubmittedAnswer

        @Serializable
        @SerialName("drag-and-drop")
        data class DragAndDrop(val questionId: Long, val mappings: List<Mapping>) : SubmittedAnswer {
            @Serializable
            data class Mapping(val dragItemId: Long, val dropLocationId: Long)
        }

        @Serializable
        @SerialName("short-answer")
        data class ShortAnswer(val questionId: Long, val submittedTexts: List<SubmittedText>) : SubmittedAnswer {
            @Serializable
            data class SubmittedText(val text: String, val spotId: Long)
        }
    }

    companion object {
        fun of(submission: QuizSubmission) = QuizSubmissionFromStudentDTO(
            submittedAnswers = submission.submittedAnswers.mapNotNull { answer ->
                val questionId = answer.quizQuestion?.id ?: return@mapNotNull null

                when (answer) {
                    is MultipleChoiceSubmittedAnswer -> SubmittedAnswer.MultipleChoice(
                        questionId = questionId,
                        selectedOptions = answer.selectedOptions.map { it.id }
                    )

                    is DragAndDropSubmittedAnswer -> SubmittedAnswer.DragAndDrop(
                        questionId = questionId,
                        mappings = answer.mappings.mapNotNull { mapping ->
                            SubmittedAnswer.DragAndDrop.Mapping(
                                dragItemId = mapping.dragItem?.id ?: return@mapNotNull null,
                                dropLocationId = mapping.dropLocation?.id ?: return@mapNotNull null
                            )
                        }
                    )

                    is ShortAnswerSubmittedAnswer -> SubmittedAnswer.ShortAnswer(
                        questionId = questionId,
                        // Artemis rejects blank texts, so the spots left empty are left out as unanswered
                        submittedTexts = answer.submittedTexts.mapNotNull { submittedText ->
                            val text = submittedText.text?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                            SubmittedAnswer.ShortAnswer.SubmittedText(
                                text = text,
                                spotId = submittedText.spot?.id ?: return@mapNotNull null
                            )
                        }
                    )
                }
            }
        )
    }
}
