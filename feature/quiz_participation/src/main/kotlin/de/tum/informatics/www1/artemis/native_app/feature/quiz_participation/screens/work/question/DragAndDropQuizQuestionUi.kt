package de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.screens.work.question

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.flowlayout.FlowRow
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.DragAndDropQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.DragAndDropQuizQuestion.DragItem
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.DragAndDropQuizQuestion.DropLocation
import io.ktor.http.HttpHeaders

// The drag and drop functionality is inspired by: https://blog.canopas.com/android-drag-and-drop-ui-element-in-jetpack-compose-14922073b3f1
// The code can be found here: https://github.com/cp-radhika-s/Drag_and_drop_jetpack_compose

private val LocalDragTargetInfo = compositionLocalOf { DragTargetInfo() }

@Composable
internal fun DragAndDropQuizQuestionUi(
    modifier: Modifier,
    questionIndex: Int,
    question: DragAndDropQuizQuestion,
    serverUrl: String,
    authToken: String,
    dropLocationMapping: Map<DropLocation, DragItem>,
    onRequestDisplayHint: () -> Unit,
    onDragItemIntoDropLocation: (itemId: Long, dropId: Long) -> Unit
) {
    Column(modifier = modifier) {
        QuizQuestionHeader(
            modifier = Modifier.fillMaxWidth(),
            questionIndex = questionIndex,
            title = question.title.orEmpty(),
            hasHint = question.hint != null,
            onRequestDisplayHint = onRequestDisplayHint
        )

        val currentDragTargetInfo = remember { DragTargetInfo() }

        var currentDropTarget: DropLocation? by remember { mutableStateOf(null) }

        CompositionLocalProvider(LocalDragTargetInfo provides currentDragTargetInfo) {
            DragAndDropDragItemsRow(
                modifier = Modifier.fillMaxWidth(),
                dragItems = question.dragItems,
                authToken = authToken,
                serverUrl = serverUrl,
                onDragRelease = { dragItem ->
                    val selectedDropLocationId = currentDropTarget?.id

                    if (selectedDropLocationId != null) {
                        onDragItemIntoDropLocation(dragItem.id, selectedDropLocationId)
                    }
                }
            )

            val backgroundFilePath = question.backgroundFilePath
            if (backgroundFilePath != null) {
                DragAndDropArea(
                    modifier = Modifier.fillMaxWidth(),
                    dropLocationMapping = dropLocationMapping,
                    imageUrl = serverUrl + backgroundFilePath,
                    dropLocations = question.dropLocations,
                    serverUrl = serverUrl,
                    authToken = authToken,
                    onUpdateIsCurrentDropTarget = { dropLocation, isCurrentDropTarget ->
                        if (!isCurrentDropTarget && dropLocation == currentDropTarget) {
                            currentDropTarget = null
                        } else if (isCurrentDropTarget) {
                            currentDropTarget = dropLocation
                        }
                    }
                )
            }
        }
    }
}

private val dragItemRowBackgroundColor: Color
    @Composable get() = if (isSystemInDarkTheme()) Color.DarkGray else Color.LightGray

private val dragItemBackgroundColor: Color
    @Composable get() = if (isSystemInDarkTheme()) Color.Black else Color.White

private val dragItemOutlineColor: Color
    @Composable get() = if (isSystemInDarkTheme()) Color.LightGray else Color.DarkGray

private val dragItemTextColor: Color
    @Composable get() = if (isSystemInDarkTheme()) Color.White else Color.Black

@Composable
private fun DragAndDropDragItemsRow(
    modifier: Modifier,
    dragItems: List<DragItem>,
    serverUrl: String,
    authToken: String,
    onDragRelease: (DragItem) -> Unit
) {
    Box(
        modifier = modifier
            .background(dragItemRowBackgroundColor)
            .zIndex(10f)
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .zIndex(10f),
            mainAxisSpacing = 8.dp,
            crossAxisSpacing = 8.dp
        ) {
            dragItems.forEach { dragItem ->
                DraggableDragItem(
                    modifier = Modifier.zIndex(10f),
                    dragItem = dragItem,
                    onDragRelease = { onDragRelease(dragItem) }
                ) {
                    DragItemUiElement(
                        modifier = Modifier,
                        text = dragItem.text,
                        pictureFilePath = dragItem.backgroundPictureServerUrl(serverUrl),
                        authToken = authToken
                    )
                }
            }
        }
    }
}


@Composable
private fun DragItemUiElement(
    modifier: Modifier,
    text: String?,
    pictureFilePath: String?,
    authToken: String
) {
    Box(
        modifier = modifier
            .background(dragItemBackgroundColor)
            .border(width = 1.dp, color = dragItemOutlineColor),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.padding(4.dp),
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
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = dragItemTextColor
                )
            }
        }

    }
}

@Composable
private fun DragAndDropArea(
    modifier: Modifier,
    dropLocationMapping: Map<DropLocation, DragItem>,
    imageUrl: String,
    dropLocations: List<DropLocation>,
    serverUrl: String,
    authToken: String,
    onUpdateIsCurrentDropTarget: (dropLocation: DropLocation, isCurrentDropTarget: Boolean) -> Unit
) {
    val context = LocalContext.current
    var loadedDrawable: Drawable? by remember { mutableStateOf(null) }

    LaunchedEffect(key1 = imageUrl) {
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .addHeader(HttpHeaders.Authorization, "Bearer $authToken")
            .target {
                loadedDrawable = it
            }
            .build()

        ImageLoader(context).execute(request)
    }

    BoxWithConstraints(modifier = modifier) {
        val currentDrawable = loadedDrawable
        if (currentDrawable != null) {
            val painter = remember { BitmapPainter(currentDrawable.toBitmap().asImageBitmap()) }
            Image(
                modifier = Modifier.fillMaxWidth(),
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.FillWidth
            )

            val imageAspectRatio =
                currentDrawable.intrinsicHeight.toFloat() / currentDrawable.intrinsicWidth.toFloat()
            val imageWidth = with(LocalDensity.current) { maxWidth.toPx() }
            val imageHeight = imageWidth * imageAspectRatio

            dropLocations.forEach { dropLocation ->
                val dragItem: DragItem? = dropLocationMapping[dropLocation]

                with(LocalDensity.current) {
                    val xPos = imageWidth * (dropLocation.posX?.toFloat() ?: 0f) / 2f / 100f
                    val yPos = imageHeight * (dropLocation.posY?.toFloat() ?: 0f) / 2f / 100f
                    val width = imageWidth * (dropLocation.width ?: 0.0).toFloat() / 2f / 100f
                    val height = imageHeight * (dropLocation.height ?: 0.0).toFloat() / 2f / 100f

                    ImageDropLocation(
                        modifier = Modifier
                            .absoluteOffset(
                                x = xPos.toDp(),
                                y = yPos.toDp()
                            )
                            .size(
                                width = width.toDp(),
                                height = height.toDp()
                            ),
                        dragItem = dragItem,
                        serverUrl = serverUrl,
                        authToken = authToken,
                        onUpdateIsCurrentDragTarget = { isCurrentDropTarget ->
                            onUpdateIsCurrentDropTarget(
                                dropLocation,
                                isCurrentDropTarget
                            )
                        }
                    )
                }
            }
        }
    }
}

private val dropTargetColorNotDragging: Color
    @Composable get() = Color.White

private val dropTargetColorDragging: Color
    @Composable get() = Color.Blue

private val dropTargetColorDropTarget: Color
    @Composable get() = Color.Green

@Composable
private fun ImageDropLocation(
    modifier: Modifier,
    dragItem: DragItem?,
    serverUrl: String,
    authToken: String,
    onUpdateIsCurrentDragTarget: (isCurrentDragTarget: Boolean) -> Unit
) {
    val dragInfo = LocalDragTargetInfo.current
    val dragPosition = dragInfo.dragPosition
    val dragOffset = dragInfo.dragOffset
    var isCurrentDropTarget by remember { mutableStateOf(false) }

    val oneDp = with(LocalDensity.current) {
        1.dp.toPx()
    }

    val stroke = remember {
        val strokeDistance = oneDp * 5f
        Stroke(
            width = oneDp,
            pathEffect = PathEffect.dashPathEffect(
                floatArrayOf(strokeDistance, strokeDistance), 0f
            )
        )
    }

    val outlineColor = dragItemOutlineColor
    val backgroundColor = when {
        isCurrentDropTarget -> dropTargetColorDropTarget
        dragInfo.isDragging -> dropTargetColorDragging
        else -> dropTargetColorNotDragging
    }

    Box(
        modifier = modifier
            .onGloballyPositioned {
                val bounds = it.boundsInWindow()
                isCurrentDropTarget = bounds.contains(dragPosition + dragOffset)

                onUpdateIsCurrentDragTarget(isCurrentDropTarget)
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(backgroundColor)

            drawRect(
                color = outlineColor, style = stroke
            )
        }

        if (dragItem != null) {
            DragItemUiElement(
                modifier = Modifier,
                text = dragItem.text,
                pictureFilePath = dragItem.backgroundPictureServerUrl(serverUrl),
                authToken = authToken
            )
        }
    }
}

@Composable
private fun DraggableDragItem(
    modifier: Modifier,
    dragItem: DragItem,
    onDragRelease: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    var currentPosition by remember { mutableStateOf(Offset.Zero) }
    val currentState = LocalDragTargetInfo.current

    var targetSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .onGloballyPositioned {
                currentPosition = it.localToWindow(Offset.Zero)
                targetSize = it.size
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        currentState.dragItem = dragItem
                        currentState.isDragging = true
                        currentState.dragPosition = currentPosition + it
                    }, onDrag = { change, dragAmount ->
                        change.consume()
                        currentState.dragOffset += Offset(dragAmount.x, dragAmount.y)
                    }, onDragEnd = {
                        currentState.isDragging = false
                        currentState.dragOffset = Offset.Zero

                        onDragRelease()
                    }, onDragCancel = {
                        currentState.dragOffset = Offset.Zero
                        currentState.isDragging = false
                    }
                )
            }
            .graphicsLayer {
                if (currentState.dragItem == dragItem && currentState.isDragging) {
                    val offset =
                        (currentState.dragPosition + currentState.dragOffset - currentPosition)
                    alpha = if (targetSize == IntSize.Zero) 0f else .9f
                    translationX = offset.x.minus(targetSize.width / 2)
                    translationY = offset.y.minus(targetSize.height / 2)
                }
            }
            .zIndex(if (currentState.isDragging) 10f else 0f),
        content = content
    )

}

internal class DragTargetInfo {
    var isDragging: Boolean by mutableStateOf(false)
    var dragPosition by mutableStateOf(Offset.Zero)
    var dragOffset by mutableStateOf(Offset.Zero)
    var dragItem by mutableStateOf<DragItem?>(null)
}

private fun DragItem.backgroundPictureServerUrl(serverUrl: String): String? =
    pictureFilePath?.let { serverUrl + it }