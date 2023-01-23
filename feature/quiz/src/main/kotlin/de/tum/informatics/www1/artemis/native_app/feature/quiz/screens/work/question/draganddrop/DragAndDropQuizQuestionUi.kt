package de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work.question.draganddrop

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.layout.times
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.*
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
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.resultMedium
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.resultSuccess
import de.tum.informatics.www1.artemis.native_app.feature.quiz.R
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizQuestionData
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work.question.QuizQuestionBodyText
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work.question.QuizQuestionHeader
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work.question.QuizQuestionInstructionText
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work.question.toQuizQuestionHeaderType
import io.ktor.http.*


@Composable
internal fun DragAndDropQuizQuestionUi(
    modifier: Modifier,
    questionIndex: Int,
    question: DragAndDropQuizQuestion,
    data: QuizQuestionData.DragAndDropData,
    serverUrl: String,
    authToken: String,
    onRequestDisplayHint: () -> Unit,
) {
    var isSampleSolutionDisplayed by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier) {
        QuizQuestionHeader(
            modifier = Modifier.fillMaxWidth(),
            questionIndex = questionIndex,
            onRequestDisplayHint = onRequestDisplayHint,
            question = question,
            type = data.toQuizQuestionHeaderType()
        )

        val currentDragTargetInfo = remember { DragInfo() }
        val dragOffset = currentDragTargetInfo.currentDragTargetInfo.dragOffset
        val dragPosition = currentDragTargetInfo.currentDragTargetInfo.dragPosition

        var currentDropTarget: DropTarget by remember { mutableStateOf(DropTarget.Nothing) }

        Column(
            modifier = Modifier.fillMaxWidth(),
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
                                && targetInfo.dragItem !in data.availableDragItems
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
                        dragItems = data.availableDragItems,
                        authToken = authToken,
                        serverUrl = serverUrl,
                        type = when (data) {
                            is QuizQuestionData.DragAndDropData.Editable -> DragAndDropDragItemsRowType.Editable(
                                onDragRelease = { dragItem ->
                                    val currentlySelectedDropTarget = currentDropTarget

                                    if (currentlySelectedDropTarget is DropTarget.DropLocationTarget) {
                                        data.onDragItemIntoDropLocation(
                                            dragItem.id,
                                            currentlySelectedDropTarget.dropLocationTarget.id
                                        )
                                    }
                                }
                            )
                            is QuizQuestionData.DragAndDropData.Result -> DragAndDropDragItemsRowType.ViewOnly
                        }
                    )

                    val imageUrl = remember(serverUrl, backgroundFilePath) {
                        URLBuilder(serverUrl)
                            .appendPathSegments(backgroundFilePath)
                            .buildString()
                    }

                    // Correct mappings as sent from the server. Not relevant for participation mode.
                    val correctMappings = remember(question.correctMappings) {
                        question.correctMappings
                            .orEmpty()
                            .mapNotNull { correctMapping ->
                                val dropLocation =
                                    correctMapping.dropLocation ?: return@mapNotNull null
                                val dragItem = correctMapping.dragItem ?: return@mapNotNull null

                                dropLocation to dragItem
                            }
                            .toMap()
                    }

                    val dropLocationMapping = if (isSampleSolutionDisplayed) {
                        correctMappings
                    } else data.dropLocationMapping

                    DragAndDropArea(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(if (isDraggingFromArea) 20f else 1f),
                        questionId = question.id,
                        dropLocationMapping = dropLocationMapping,
                        imageUrl = imageUrl,
                        dropLocations = question.dropLocations,
                        serverUrl = serverUrl,
                        authToken = authToken,
                        type = when (data) {
                            is QuizQuestionData.DragAndDropData.Editable -> {
                                DragAndDropAreaType.Editable(
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
                                            currentDropTarget =
                                                DropTarget.DropLocationTarget(dropLocation)
                                        }
                                    },
                                    onDragRelease = { originalDropLocation ->
                                        when (val currentlySelectedDropTarget = currentDropTarget) {
                                            is DropTarget.DropLocationTarget -> {
                                                // trigger swap
                                                data.onSwapDropLocations(
                                                    originalDropLocation.id,
                                                    currentlySelectedDropTarget.dropLocationTarget.id
                                                )
                                            }

                                            DropTarget.AvailableItemsRow -> {
                                                // trigger clear
                                                data.onClearDropLocation(originalDropLocation.id)
                                            }

                                            // Do nothing here.
                                            DropTarget.Nothing -> {}
                                        }
                                    }
                                )
                            }
                            is QuizQuestionData.DragAndDropData.Result -> DragAndDropAreaType.ViewOnly(
                                isSampleSolution = isSampleSolutionDisplayed,
                                correctMappings = correctMappings
                            )
                        }
                    )

                    if (data is QuizQuestionData.DragAndDropData.Result) {
                        val buttonText = if (isSampleSolutionDisplayed) {
                            R.string.quiz_result_dnd_hide_sample_solution
                        } else R.string.quiz_result_dnd_show_sample_solution

                        OutlinedButton(
                            modifier = Modifier.padding(vertical = 8.dp),
                            onClick = { isSampleSolutionDisplayed = !isSampleSolutionDisplayed }
                        ) {
                            Text(text = stringResource(id = buttonText))
                        }
                    }
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

sealed interface DragAndDropDragItemsRowType {
    object ViewOnly : DragAndDropDragItemsRowType

    data class Editable(val onDragRelease: (DragItem) -> Unit) : DragAndDropDragItemsRowType
}

@Composable
private fun DragAndDropDragItemsRow(
    modifier: Modifier,
    dragItems: List<DragItem>,
    serverUrl: String,
    authToken: String,
    type: DragAndDropDragItemsRowType
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
                val dragItemUiElement = @Composable {
                    DragItemUiElement(
                        modifier = Modifier,
                        text = dragItem.text,
                        pictureFilePath = dragItem.backgroundPictureServerUrl(serverUrl),
                        authToken = authToken
                    )
                }

                when (type) {
                    is DragAndDropDragItemsRowType.Editable -> {
                        DragItemDraggableContainer(
                            modifier = Modifier.zIndex(10f),
                            dragItem = dragItem,
                            onDragRelease = { type.onDragRelease(dragItem) }
                        ) {
                            dragItemUiElement()
                        }
                    }
                    DragAndDropDragItemsRowType.ViewOnly -> {
                        dragItemUiElement()
                    }
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

private sealed interface DragAndDropAreaType {
    data class ViewOnly(
        val isSampleSolution: Boolean,
        val correctMappings: Map<DropLocation, DragItem>
    ) : DragAndDropAreaType

    data class Editable(
        val onUpdateIsCurrentDropTarget: (dropLocation: DropLocation, isCurrentDropTarget: Boolean) -> Unit,
        val onDragRelease: (originalDropLocation: DropLocation) -> Unit
    ) : DragAndDropAreaType
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
    type: DragAndDropAreaType
) {
    val context = LocalContext.current

    val request = remember {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .memoryCacheKey("QQ_$questionId")
            .addHeader(HttpHeaders.Cookie, "jwt=$authToken")
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
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            var composableSize by remember { mutableStateOf(IntSize.Zero) }

            val painter = remember { BitmapPainter(loadedDrawable.toBitmap().asImageBitmap()) }
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned {
                        composableSize = it.size
                    },
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

                    val composableWidth = composableSize.width.toDp()
                    val composableHeight = composableSize.height.toDp()

                    ImageDropLocation(
                        modifier = Modifier
                            .absoluteOffset(
                                x = composableWidth / 2 - actImageSize.width.toDp() / 2 + xPos.toDp(),
                                y = composableHeight / 2 - actImageSize.height.toDp() / 2 + yPos.toDp()
                            )
                            .size(
                                width = width.toDp(),
                                height = height.toDp()
                            ),
                        dragItem = dragItem,
                        serverUrl = serverUrl,
                        authToken = authToken,
                        type = when (type) {
                            is DragAndDropAreaType.Editable -> {
                                ImageDropLocationType.Editable(
                                    onUpdateIsCurrentDragTarget = { isCurrentDropTarget ->
                                        type.onUpdateIsCurrentDropTarget(
                                            dropLocation,
                                            isCurrentDropTarget
                                        )
                                    },
                                    onDragRelease = {
                                        if (dragItem != null) type.onDragRelease(dropLocation)
                                    }
                                )
                            }
                            is DragAndDropAreaType.ViewOnly -> {
                                val isCorrect =
                                    type.correctMappings[dropLocation] == dropLocationMapping[dropLocation]
                                ImageDropLocationType.ViewOnly(
                                    isCorrect = isCorrect,
                                    isDisplayingSampleSolution = type.isSampleSolution
                                )
                            }
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

private sealed interface ImageDropLocationType {
    data class ViewOnly(val isCorrect: Boolean, val isDisplayingSampleSolution: Boolean) :
        ImageDropLocationType

    data class Editable(
        val onUpdateIsCurrentDragTarget: (isCurrentDragTarget: Boolean) -> Unit,
        val onDragRelease: () -> Unit
    ) : ImageDropLocationType
}

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
    type: ImageDropLocationType
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
        is ImageDropLocationType.Editable -> dragItemOutlineColor
        is ImageDropLocationType.ViewOnly -> {
            if (type.isDisplayingSampleSolution) {
                dragItemOutlineColor
            } else {
                if (type.isCorrect) resultSuccess
                else resultMedium
            }
        }
    }
    
    val backgroundColor: Color = when (type) {
        is ImageDropLocationType.Editable -> when {
            isCurrentDropTarget -> dropTargetColorDropTarget
            targetInfo is DragTargetInfo.Dragging -> dropTargetColorDragging
            else -> dropTargetColorNotDragging
        }
        is ImageDropLocationType.ViewOnly -> {
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
                if (type is ImageDropLocationType.Editable) {
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
                is ImageDropLocationType.Editable -> {
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
                is ImageDropLocationType.ViewOnly -> {
                    nonDraggedElementContent()
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