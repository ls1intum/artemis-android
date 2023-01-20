package de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.screens.work.question.draganddrop

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.flowlayout.FlowRow
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.DragAndDropQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.DragAndDropQuizQuestion.DragItem
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.DragAndDropQuizQuestion.DropLocation
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.common.image.loadAsyncImageDrawable
import de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.R
import de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.screens.work.question.QuizQuestionBodyText
import de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.screens.work.question.QuizQuestionHeader
import de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.screens.work.question.QuizQuestionInstructionText
import io.ktor.http.HttpHeaders
import androidx.compose.ui.layout.times


@Composable
internal fun DragAndDropQuizQuestionUi(
    modifier: Modifier,
    questionIndex: Int,
    question: DragAndDropQuizQuestion,
    availableDragItems: List<DragItem>,
    serverUrl: String,
    authToken: String,
    dropLocationMapping: Map<DropLocation, DragItem>,
    onRequestDisplayHint: () -> Unit,
    onDragItemIntoDropLocation: (itemId: Long, dropId: Long) -> Unit,
    onSwapDropLocations: (oldDropLocationId: Long, newDropLocationId: Long) -> Unit,
    onClearDropLocation: (dropLocationId: Long) -> Unit
) {
    Column(modifier = modifier) {
        QuizQuestionHeader(
            modifier = Modifier.fillMaxWidth(),
            questionIndex = questionIndex,
            onRequestDisplayHint = onRequestDisplayHint,
            question = question
        )

        val currentDragTargetInfo = remember { DragInfo() }
        val dragOffset = currentDragTargetInfo.currentDragTargetInfo.dragOffset
        val dragPosition = currentDragTargetInfo.currentDragTargetInfo.dragPosition

        var currentDropTarget: DropTarget by remember { mutableStateOf(DropTarget.Nothing) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuizQuestionBodyText(
                modifier = Modifier.fillMaxWidth(),
                question = question
            )

            CompositionLocalProvider(LocalDragTargetInfo provides currentDragTargetInfo) {
                val backgroundFilePath = question.backgroundFilePath
                if (backgroundFilePath != null) {
                    // If there is currently a drag happening and it is originating from
                    // one of the drop locations inside this area.
                    val targetInfo = currentDragTargetInfo.currentDragTargetInfo
                    val isDraggingFromArea = remember(targetInfo) {
                        targetInfo is DragTargetInfo.Dragging
                                && targetInfo.dragItem !in availableDragItems
                    }

                    QuizQuestionInstructionText(
                        modifier = Modifier.fillMaxWidth(),
                        instructionText = stringResource(id = R.string.quiz_participation_drag_and_drop_instruction)
                    )

                    DragAndDropDragItemsRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 60.dp)
                            .onGloballyPositioned {
                                val bounds = it.boundsInWindow()
                                val isDropTarget =
                                    currentDragTargetInfo.currentDragTargetInfo is DragTargetInfo.Dragging &&
                                            bounds.contains(dragOffset + dragPosition)

                                if (currentDropTarget == DropTarget.AvailableItemsRow && !isDropTarget) {
                                    currentDropTarget = DropTarget.Nothing
                                } else if (isDropTarget) {
                                    currentDropTarget = DropTarget.AvailableItemsRow
                                }
                            },
                        dragItems = availableDragItems,
                        authToken = authToken,
                        serverUrl = serverUrl,
                        onDragRelease = { dragItem ->
                            val currentlySelectedDropTarget = currentDropTarget

                            if (currentlySelectedDropTarget is DropTarget.DropLocationTarget) {
                                onDragItemIntoDropLocation(
                                    dragItem.id,
                                    currentlySelectedDropTarget.dropLocationTarget.id
                                )
                            }
                        }
                    )

                    DragAndDropArea(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(if (isDraggingFromArea) 20f else 1f),
                        questionId = question.id,
                        dropLocationMapping = dropLocationMapping,
                        imageUrl = serverUrl + backgroundFilePath,
                        dropLocations = question.dropLocations,
                        serverUrl = serverUrl,
                        authToken = authToken,
                        onUpdateIsCurrentDropTarget = { dropLocation, isCurrentDropTarget ->
                            val currentlySetDropTarget = currentDropTarget

                            // Set drop location to nothing if it is currently set to this drop location target.
                            // If it is the current drop target, set it.
                            if (
                                !isCurrentDropTarget && currentlySetDropTarget is DropTarget.DropLocationTarget
                                && currentlySetDropTarget.dropLocationTarget == dropLocation
                            ) {
                                currentDropTarget = DropTarget.Nothing
                            } else if (isCurrentDropTarget) {
                                currentDropTarget = DropTarget.DropLocationTarget(dropLocation)
                            }
                        },
                        onDragRelease = { originalDropLocation ->
                            when (val currentlySelectedDropTarget = currentDropTarget) {
                                is DropTarget.DropLocationTarget -> {
                                    // trigger swap
                                    onSwapDropLocations(
                                        originalDropLocation.id,
                                        currentlySelectedDropTarget.dropLocationTarget.id
                                    )
                                }

                                DropTarget.AvailableItemsRow -> {
                                    // trigger clear
                                    onClearDropLocation(originalDropLocation.id)
                                }

                                // Do nothing here.
                                DropTarget.Nothing -> {}
                            }
                        }
                    )
                }
            }
        }
    }
}

private val dragItemRowBackgroundColor: Color
    @Composable get() = if (isSystemInDarkTheme()) Color.DarkGray else Color.LightGray

private val dragItemBackgroundColor: Color
    @Composable get() = if (isSystemInDarkTheme()) Color.Black else Color.White

private val dragItemOutlineColor: Color
    @Composable get() = Color.DarkGray

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
                DragItemDraggableContainer(
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
private fun DragItemUiElementContent(
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
                color = fontColor,
                fontSize = fontSize
            )
        }
    }
}

/**
 * Load and display the background image. Renders the drop locations onto the image and reports
 * drag events about drag items already placed in the drop locations.
 */
@Composable
private fun DragAndDropArea(
    modifier: Modifier,
    questionId: Long,
    dropLocationMapping: Map<DropLocation, DragItem>,
    imageUrl: String,
    dropLocations: List<DropLocation>,
    serverUrl: String,
    authToken: String,
    onUpdateIsCurrentDropTarget: (dropLocation: DropLocation, isCurrentDropTarget: Boolean) -> Unit,
    onDragRelease: (originalDropLocation: DropLocation) -> Unit
) {
    val context = LocalContext.current

    val request = remember {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .memoryCacheKey("QQ_$questionId")
            .addHeader(HttpHeaders.Authorization, "Bearer $authToken")
            .build()
    }

    val resultData = loadAsyncImageDrawable(request = request)

    BasicDataStateUi(
        modifier = modifier,
        dataState = resultData.dataState,
        loadingText = stringResource(id = R.string.quiz_participation_load_dnd_image_loading),
        failureText = stringResource(id = R.string.quiz_participation_load_dnd_image_failure),
        retryButtonText = stringResource(id = R.string.quiz_participation_load_dnd_image_retry),
        onClickRetry = resultData.requestRetry
    ) { loadedDrawable ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val painter = remember { BitmapPainter(loadedDrawable.toBitmap().asImageBitmap()) }
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Fit
            )

            val srcSize = Size(
                loadedDrawable.intrinsicWidth.toFloat(),
                loadedDrawable.intrinsicHeight.toFloat()
            )

            val dstSize = with(LocalDensity.current) {
                Size(
                    maxWidth.toPx(),
                    maxHeight.toPx()
                )
            }

            val scaleFactor = ContentScale.Fit.computeScaleFactor(srcSize, dstSize)
            val actImageSize = srcSize * scaleFactor

            val (imageWidth: Float, imageHeight: Float) = actImageSize

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
                                x = maxWidth / 2 - actImageSize.width.toDp() / 2 + xPos.toDp(),
                                y = maxHeight / 2 - actImageSize.height.toDp() / 2 + yPos.toDp()
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
                        },
                        onDragRelease = {
                            if (dragItem != null) onDragRelease(dropLocation)
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

/**
 * The drop location inside the background image.
 * Draws an outline and a background color into the image. If the drop location holds a drag item,
 * the drag item is displayed. Handles dragging of the drag item it holds.
 */
@Composable
private fun ImageDropLocation(
    modifier: Modifier,
    dragItem: DragItem?,
    serverUrl: String,
    authToken: String,
    onUpdateIsCurrentDragTarget: (isCurrentDragTarget: Boolean) -> Unit,
    onDragRelease: () -> Unit
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

    val outlineColor = dragItemOutlineColor
    val backgroundColor = when {
        isCurrentDropTarget -> dropTargetColorDropTarget
        targetInfo is DragTargetInfo.Dragging -> dropTargetColorDragging
        else -> dropTargetColorNotDragging
    }

    val isDraggingDragChild =
        targetInfo is DragTargetInfo.Dragging && targetInfo.dragItem == dragItem

    Box(
        modifier = modifier
            .onGloballyPositioned {
                val bounds = it.boundsInWindow()
                val wasCurrentDropTarget = isCurrentDropTarget

                isCurrentDropTarget =
                    targetInfo is DragTargetInfo.Dragging && bounds.contains(dragPosition + dragOffset)

                // Update either when it is the current drag target or it previously was and no longer is now.
                if (isCurrentDropTarget || wasCurrentDropTarget) {
                    onUpdateIsCurrentDragTarget(isCurrentDropTarget)
                }
            }
            .zIndex(if (isDraggingDragChild) 20f else 0f)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(backgroundColor)

            drawRect(
                color = outlineColor, style = stroke
            )
        }

        if (dragItem != null) {
            val pictureFilePath = dragItem.backgroundPictureServerUrl(serverUrl)

            DragItemDraggableContainer(
                modifier = Modifier.fillMaxSize(),
                dragItem = dragItem,
                onDragRelease = onDragRelease
            ) {
                if (isDraggingDragChild) {
                    DragItemUiElement(
                        modifier = Modifier.zIndex(20f),
                        text = dragItem.text,
                        pictureFilePath = pictureFilePath,
                        authToken = authToken
                    )
                } else {
                    DragItemUiElementContent(
                        modifier = Modifier.fillMaxSize(),
                        text = dragItem.text,
                        pictureFilePath = pictureFilePath,
                        authToken = authToken,
                        fontColor = Color.Black
                    )
                }
            }
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
    dragItem: DragItem,
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
    fontSize: TextUnit,
    maxLines: Int = Int.MAX_VALUE,
    color: Color = Color.Unspecified
) {
    var textSize by remember { mutableStateOf(fontSize) }

    Text(
        modifier = modifier,
        text = text,
        style = style,
        maxLines = maxLines,
        color = color,
        fontSize = textSize,
        onTextLayout = { result ->
            if (result.isLineEllipsized(result.lineCount - 1)) {
                textSize *= 0.9
            }
        }
    )
}

private fun DragItem.backgroundPictureServerUrl(serverUrl: String): String? =
    pictureFilePath?.let { serverUrl + it }