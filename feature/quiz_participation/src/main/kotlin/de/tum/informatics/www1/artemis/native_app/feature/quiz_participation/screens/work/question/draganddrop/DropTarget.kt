package de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.screens.work.question.draganddrop

import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.DragAndDropQuizQuestion

sealed interface DropTarget {
    object Nothing : DropTarget
    object AvailableItemsRow : DropTarget
    data class DropLocationTarget(
        val dropLocationTarget: DragAndDropQuizQuestion.DropLocation
    ) : DropTarget
}