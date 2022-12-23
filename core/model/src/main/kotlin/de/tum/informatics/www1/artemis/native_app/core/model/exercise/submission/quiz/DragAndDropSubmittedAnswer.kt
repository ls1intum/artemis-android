package de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz

import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.DragAndDropQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.QuizQuestion
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SerialName("drag-and-drop")
@Serializable
class DragAndDropSubmittedAnswer(
    override val id: Long? = null,
    override val scoreInPoints: Double? = null,
    override val quizQuestion: QuizQuestion? = null,
    val mappings: List<DragAndDropMapping> = emptyList()
) : SubmittedAnswer() {
    @Serializable
    data class DragAndDropMapping(
        val id: Long? = null,
        val dragItemIndex: Long? = null,
        val dropLocationIndex: Long? = null,
        val invalid: Boolean = false,
        val dragItem: DragAndDropQuizQuestion.DragItem?,
        val dropLocation: DragAndDropQuizQuestion.DropLocation?
    )
}