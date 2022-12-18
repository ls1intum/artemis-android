package de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("drag-and-drop")
data class DragAndDropQuizQuestion(
    override val id: Long? = null,
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
) : QuizQuestion() {

    @Serializable
    data class DropLocation(
        val posX: Double? = null,
        val posY: Double? = null,
        val width: Double? = null,
        val height: Double? = null,
        val invalid: Boolean = false
    )

    @Serializable
    data class DragItem(
        val pictureFilePath: String? = null,
        val text: String? = null,
        val invalid: Boolean = false
    )
}