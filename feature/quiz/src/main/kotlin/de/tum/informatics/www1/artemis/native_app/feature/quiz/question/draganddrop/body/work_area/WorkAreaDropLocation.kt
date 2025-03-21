package de.tum.informatics.www1.artemis.native_app.feature.quiz.question.draganddrop.body.work_area

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.DragAndDropQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.ui.material.colors.ExerciseColors
import de.tum.informatics.www1.artemis.native_app.feature.quiz.question.draganddrop.DragTargetInfo
import de.tum.informatics.www1.artemis.native_app.feature.quiz.question.draganddrop.LocalDragTargetInfo
import de.tum.informatics.www1.artemis.native_app.feature.quiz.question.draganddrop.body.DragItemDraggableContainer
import de.tum.informatics.www1.artemis.native_app.feature.quiz.question.draganddrop.body.DragItemUiElement
import de.tum.informatics.www1.artemis.native_app.feature.quiz.question.draganddrop.body.DragItemUiElementContent
import de.tum.informatics.www1.artemis.native_app.feature.quiz.question.draganddrop.body.dragItemOutlineColor
import de.tum.informatics.www1.artemis.native_app.feature.quiz.question.draganddrop.dragOffset
import de.tum.informatics.www1.artemis.native_app.feature.quiz.question.draganddrop.dragPosition

internal sealed interface WorkAreaDropLocationType {
    data class ViewOnly(val isCorrect: Boolean, val isDisplayingSampleSolution: Boolean) :
        WorkAreaDropLocationType

    data class Editable(
        val onUpdateIsCurrentDragTarget: (isCurrentDragTarget: Boolean) -> Unit,
        val onDragRelease: () -> Unit
    ) : WorkAreaDropLocationType
}

/**
 * The drop location inside the background image.
 * Draws an outline and a background color into the image. If the drop location holds a drag item,
 * the drag item is displayed. Handles dragging of the drag item it holds.
 */
@Composable
internal fun WorkAreaDropLocation(
    modifier: Modifier,
    dragItem: DragAndDropQuizQuestion.DragItem?,
    type: WorkAreaDropLocationType
) {
    val targetInfo = LocalDragTargetInfo.current.currentDragTargetInfo
    val dragPosition = targetInfo.dragPosition
    val dragOffset = targetInfo.dragOffset

    var isCurrentDropTarget by remember { mutableStateOf(false) }

    val outlineWidth = with(LocalDensity.current) {
        3.dp.toPx()
    }

    val stroke = remember {
        val strokeDistance = outlineWidth * 2f
        Stroke(
            width = outlineWidth,
            pathEffect = PathEffect.dashPathEffect(
                floatArrayOf(strokeDistance, strokeDistance), 0f
            )
        )
    }

    val outlineColor = when (type) {
        is WorkAreaDropLocationType.Editable -> dragItemOutlineColor
        is WorkAreaDropLocationType.ViewOnly -> {
            if (type.isDisplayingSampleSolution) {
                dragItemOutlineColor
            } else {
                if (type.isCorrect) ExerciseColors.Result.success
                else ExerciseColors.Result.medium
            }
        }
    }

    val backgroundColor: Color = when (type) {
        is WorkAreaDropLocationType.Editable -> when {
            isCurrentDropTarget -> ExerciseColors.Quiz.dropTargetColorDropTarget
            targetInfo is DragTargetInfo.Dragging -> ExerciseColors.Quiz.dropTargetColorDragging
            else -> ExerciseColors.Quiz.dropTargetColorNotDragging
        }
        is WorkAreaDropLocationType.ViewOnly -> {
            if (type.isDisplayingSampleSolution) {
                ExerciseColors.Quiz.dropTargetColorNotDragging
            } else {
                if (type.isCorrect) ExerciseColors.Result.success.copy(alpha = 0.2f)
                else ExerciseColors.Result.medium.copy(alpha = 0.2f)
            }
        }
    }

    val isDraggingDragChild =
        targetInfo is DragTargetInfo.Dragging && targetInfo.dragItem == dragItem

    Box(
        modifier = modifier
            .let {
                // Only needed in edit mode
                if (type is WorkAreaDropLocationType.Editable) {
                    it
                        .onGloballyPositioned { layoutCoordinates ->
                            val bounds = layoutCoordinates.boundsInWindow()
                            val wasCurrentDropTarget = isCurrentDropTarget

                            isCurrentDropTarget =
                                targetInfo is DragTargetInfo.Dragging && bounds.contains(
                                    dragPosition + dragOffset
                                )

                            // Update either when it is the current drag target or it previously was and no longer is now.
                            if (isCurrentDropTarget || wasCurrentDropTarget) {
                                type.onUpdateIsCurrentDragTarget(isCurrentDropTarget)
                            }
                        }
                        .zIndex(if (isDraggingDragChild) 20f else 0f)
                } else it
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(backgroundColor)

            drawRect(
                color = outlineColor, style = stroke
            )
        }

        if (dragItem != null) {
            val nonDraggedElementContent = @Composable {
                DragItemUiElementContent(
                    modifier = Modifier.fillMaxSize(),
                    text = dragItem.text,
                    pictureFilePath = dragItem.pictureFilePath,
                    fontColor = Color.Black
                )
            }

            when (type) {
                is WorkAreaDropLocationType.Editable -> {
                    DragItemDraggableContainer(
                        modifier = Modifier.fillMaxSize(),
                        dragItem = dragItem,
                        onDragRelease = type.onDragRelease
                    ) {
                        if (isDraggingDragChild) {
                            DragItemUiElement(
                                modifier = Modifier.zIndex(20f),
                                text = dragItem.text,
                                pictureFilePath = dragItem.pictureFilePath,
                            )
                        } else {
                            nonDraggedElementContent()
                        }
                    }
                }
                is WorkAreaDropLocationType.ViewOnly -> {
                    nonDraggedElementContent()
                }
            }
        }
    }
}