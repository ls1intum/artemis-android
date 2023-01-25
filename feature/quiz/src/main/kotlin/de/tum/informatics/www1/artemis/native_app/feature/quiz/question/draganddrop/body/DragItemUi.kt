package de.tum.informatics.www1.artemis.native_app.feature.quiz.question.draganddrop.body

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.ImageRequest
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.DragAndDropQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.ui.common.AutoResizeText
import de.tum.informatics.www1.artemis.native_app.core.ui.common.FontSizeRange
import de.tum.informatics.www1.artemis.native_app.feature.quiz.question.draganddrop.DragTargetInfo
import de.tum.informatics.www1.artemis.native_app.feature.quiz.question.draganddrop.LocalDragTargetInfo
import io.ktor.http.*

private val dragItemBackgroundColor: Color
    @Composable get() = if (isSystemInDarkTheme()) Color.Black else Color.White

private val dragItemTextColor: Color
    @Composable get() = if (isSystemInDarkTheme()) Color.White else Color.Black

@Composable
internal fun DragItemUiElement(
    modifier: Modifier,
    text: String?,
    pictureFilePath: String?,
    authToken: String,
    fontSize: TextUnit = 18.sp
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
            authToken = authToken,
            fontSize = fontSize
        )
    }
}

@Composable
internal fun DragItemUiElementContent(
    modifier: Modifier,
    text: String?,
    pictureFilePath: String?,
    authToken: String,
    fontColor: Color = dragItemTextColor,
    fontSize: TextUnit = TextUnit.Unspecified
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (pictureFilePath != null) {
            val request = ImageRequest.Builder(LocalContext.current)
                .data(pictureFilePath)
                .addHeader(HttpHeaders.Authorization, "Bearer $authToken")
                .build()

            AsyncImage(
                model = request, contentDescription = null,
                modifier = Modifier
                    .widthIn(max = 60.dp)
                    .heightIn(max = 60.dp)
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
                        onDragRelease()
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