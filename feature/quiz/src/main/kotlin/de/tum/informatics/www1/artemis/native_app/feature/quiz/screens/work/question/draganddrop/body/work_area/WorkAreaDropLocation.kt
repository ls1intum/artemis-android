package de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work.question.draganddrop.body.work_area

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
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
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.resultMedium
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.resultSuccess
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work.question.draganddrop.DragTargetInfo
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work.question.draganddrop.LocalDragTargetInfo
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work.question.draganddrop.body.*
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work.question.draganddrop.body.DragItemDraggableContainer
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work.question.draganddrop.body.DragItemUiElement
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work.question.draganddrop.body.DragItemUiElementContent
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work.question.draganddrop.body.backgroundPictureServerUrl
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work.question.draganddrop.body.dragItemOutlineColor
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work.question.draganddrop.dragOffset
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work.question.draganddrop.dragPosition

private val dropTargetColorNotDragging: Color
    @Composable get() = Color.White

private val dropTargetColorDragging: Color
    @Composable get() = Color.Blue

private val dropTargetColorDropTarget: Color
    @Composable get() = Color.Green

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
    serverUrl: String,
    authToken: String,
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
                if (type.isCorrect) resultSuccess
                else resultMedium
            }
        }
    }

    val backgroundColor: Color = when (type) {
        is WorkAreaDropLocationType.Editable -> when {
            isCurrentDropTarget -> dropTargetColorDropTarget
            targetInfo is DragTargetInfo.Dragging -> dropTargetColorDragging
            else -> dropTargetColorNotDragging
        }
        is WorkAreaDropLocationType.ViewOnly -> {
            if (type.isDisplayingSampleSolution) {
                dropTargetColorNotDragging
            } else {
                if (type.isCorrect) resultSuccess.copy(alpha = 0.2f)
                else resultMedium.copy(alpha = 0.2f)
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
            val pictureFilePath = dragItem.backgroundPictureServerUrl(serverUrl)

            val nonDraggedElementContent = @Composable {
                DragItemUiElementContent(
                    modifier = Modifier.fillMaxSize(),
                    text = dragItem.text,
                    pictureFilePath = pictureFilePath,
                    authToken = authToken,
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
                                pictureFilePath = pictureFilePath,
                                authToken = authToken
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