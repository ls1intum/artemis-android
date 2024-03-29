package de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz

import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.DragAndDropSubmittedAnswer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("drag-and-drop")
data class DragAndDropQuizQuestion(
    override val id: Long = 0,
    override val title: String? = null,
    override val text: String? = null,
    override val hint: String? = null,
    override val explanation: String? = null,
    override val points: Int? = null,
    override val scoringType: ScoringType? = null,
    override val randomizeOrder: Boolean = true,
    override val invalid: Boolean = false,
    val backgroundFilePath: String? = null,
    val dropLocations: List<DropLocation> = emptyList(),
    val dragItems: List<DragItem> = emptyList(),
    val correctMappings: List<DragAndDropSubmittedAnswer.DragAndDropMapping>? = null
) : QuizQuestion() {

    val dropLocationById: Map<Long, DropLocation> = dropLocations.associateBy { it.id }
    val dragItemById: Map<Long, DragItem> = dragItems.associateBy { it.id }

    @Serializable
    data class DropLocation(
        val id: Long = 0,
        val posX: Double? = null,
        val posY: Double? = null,
        val width: Double? = null,
        val height: Double? = null,
        val invalid: Boolean = false
    )

    @Serializable
    data class DragItem(
        val id: Long = 0,
        val pictureFilePath: String? = null,
        val text: String? = null,
        val invalid: Boolean = false
    )
}