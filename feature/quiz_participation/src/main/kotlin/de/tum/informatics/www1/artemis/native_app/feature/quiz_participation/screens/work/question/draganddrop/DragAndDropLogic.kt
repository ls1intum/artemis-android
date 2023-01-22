package de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.screens.work.question.draganddrop

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.DragAndDropQuizQuestion

// The drag and drop functionality is inspired by: https://blog.canopas.com/android-drag-and-drop-ui-element-in-jetpack-compose-14922073b3f1
// The code can be found here: https://github.com/cp-radhika-s/Drag_and_drop_jetpack_compose

internal val LocalDragTargetInfo = compositionLocalOf { DragInfo() }

internal class DragInfo {
    var currentDragTargetInfo: DragTargetInfo by mutableStateOf(DragTargetInfo.NotDragging)
}

internal sealed interface DragTargetInfo {
    object NotDragging : DragTargetInfo

    class Dragging(
        val dragItem: DragAndDropQuizQuestion.DragItem
    ) : DragTargetInfo {
        var dragPosition by mutableStateOf(Offset.Zero)
        var dragOffset by mutableStateOf(Offset.Zero)
    }
}

internal val DragTargetInfo.dragPosition: Offset
    @Composable get() = when (this) {
        is DragTargetInfo.Dragging -> dragPosition
        DragTargetInfo.NotDragging -> Offset.Zero
    }

internal val DragTargetInfo.dragOffset: Offset
    @Composable get() = when (this) {
        is DragTargetInfo.Dragging -> dragOffset
        DragTargetInfo.NotDragging -> Offset.Zero
    }