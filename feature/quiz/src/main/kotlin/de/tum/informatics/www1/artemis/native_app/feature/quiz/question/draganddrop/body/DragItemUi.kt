package de.tum.informatics.www1.artemis.native_app.feature.quiz.question.draganddrop.body

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.DragAndDropQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.ui.common.AutoResizeText
import de.tum.informatics.www1.artemis.native_app.core.ui.common.FontSizeRange
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.LocalArtemisImageProvider
import de.tum.informatics.www1.artemis.native_app.feature.quiz.question.draganddrop.DragTargetInfo
import de.tum.informatics.www1.artemis.native_app.feature.quiz.question.draganddrop.LocalDragTargetInfo

private val dragItemBackgroundColor: Color
    @Composable get() = if (isSystemInDarkTheme()) Color.Black else Color.White

private val dragItemTextColor: Color
    @Composable get() = if (isSystemInDarkTheme()) Color.White else Color.Black

@Composable
internal fun DragItemUiElement(
    modifier: Modifier,
    text: String?,
    pictureFilePath: String?,
) {
    Box(
        modifier = modifier
            .background(dragItemBackgroundColor)
            .border(width = 1.dp, color = dragItemOutlineColor),
        contentAlignment = Alignment.Center
    ) {
        DragItemUiElementContent(
            modifier = Modifier.padding(4.dp),
            text = text,
            pictureFilePath = pictureFilePath,
        )
    }
}

@Composable
internal fun DragItemUiElementContent(
    modifier: Modifier,
    text: String?,
    pictureFilePath: String?,
    fontColor: Color = dragItemTextColor
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (pictureFilePath != null) {
            val asyncImagePainter = LocalArtemisImageProvider.current.rememberArtemisAsyncImagePainter(
                imagePath = pictureFilePath
            )

            Image(
                modifier = Modifier
                    .widthIn(max = 60.dp)
                    .heightIn(max = 60.dp),
                painter = asyncImagePainter,
                contentDescription = null
            )
        }

        if (text != null) {
            AutosizeText(
                modifier = Modifier,
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = fontColor
            )
        }
    }
}

/**
 * A container for a draggable composable that is made up of a DragItem.
 * When the drag on this composable is started/ended, it updates LocalDragTargetInfo.
 */
@Composable
internal fun DragItemDraggableContainer(
    modifier: Modifier,
    dragItem: DragAndDropQuizQuestion.DragItem,
    onDragRelease: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    var currentPosition by remember(dragItem) { mutableStateOf(Offset.Zero) }
    val currentDragTargetInfo = LocalDragTargetInfo.current
    // The drag target info currently set
    val currentTargetInfo = currentDragTargetInfo.currentDragTargetInfo

    // A drag target info representing this drag item
    val selfTargetInfo: DragTargetInfo.Dragging by remember(dragItem) {
        mutableStateOf(DragTargetInfo.Dragging(dragItem))
    }

    var targetSize by remember(dragItem) { mutableStateOf(IntSize.Zero) }

    val onDragDone = {
        selfTargetInfo.dragOffset = Offset.Zero
        selfTargetInfo.dragPosition = Offset.Zero

        currentDragTargetInfo.currentDragTargetInfo = DragTargetInfo.NotDragging
    }

    // We need to wrap the onDragRelease in a wrapper so that pointInput can always access the latest one
    // We cannot put onDragRelease as a restart parameter of pointer input, as then the drag and drop no longer works
    val onDragReleaseWrapper = remember { mutableStateOf(onDragRelease) }
    onDragReleaseWrapper.value = onDragRelease

    Box(
        modifier = modifier
            .onGloballyPositioned {
                currentPosition = it.localToWindow(Offset.Zero)
                targetSize = it.size
            }
            .pointerInput(dragItem) {
                detectDragGestures(
                    onDragStart = {
                        currentDragTargetInfo.currentDragTargetInfo = selfTargetInfo
                        selfTargetInfo.dragPosition = currentPosition + it
                    }, onDrag = { change, dragAmount ->
                        change.consume()
                        selfTargetInfo.dragOffset += Offset(dragAmount.x, dragAmount.y)
                    }, onDragEnd = {
                        onDragDone()
                        onDragReleaseWrapper.value.invoke()
                    }, onDragCancel = {
                        onDragDone()
                    }
                )
            }
            .graphicsLayer {
                if (currentTargetInfo == selfTargetInfo) {
                    val offset =
                        (selfTargetInfo.dragPosition + selfTargetInfo.dragOffset - currentPosition)
                    alpha = if (targetSize == IntSize.Zero) 0f else .9f
                    translationX = offset.x.minus(targetSize.width / 2)
                    translationY = offset.y.minus(targetSize.height / 2)
                }
            }
            .zIndex(if (currentTargetInfo == selfTargetInfo) 10f else 0f),
        content = content
    )
}

@Composable
private fun AutosizeText(
    modifier: Modifier,
    text: String,
    style: TextStyle,
    maxLines: Int = Int.MAX_VALUE,
    color: Color = Color.Unspecified
) {
    AutoResizeText(
        modifier = modifier,
        text = text,
        fontSizeRange = FontSizeRange(min = 8.sp, max = 24.sp, step = 1.sp),
        color = color,
        style = style,
        maxLines = maxLines
    )
}