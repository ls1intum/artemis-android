package de.tum.informatics.www1.artemis.native_app.feature.quiz.question.draganddrop.body.work_area

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.times
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import androidx.core.graphics.scale
import coil3.toBitmap
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.DragAndDropQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.common.image.loadAsyncImageDrawable
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.BaseImageProviderImpl
import de.tum.informatics.www1.artemis.native_app.feature.quiz.R

internal sealed interface DragAndDropAreaType {
    data class ViewOnly(
        val isSampleSolution: Boolean,
        val sampleSolutionMappings: Map<DragAndDropQuizQuestion.DropLocation, DragAndDropQuizQuestion.DragItem>
    ) : DragAndDropAreaType

    data class Editable(
        val onUpdateIsCurrentDropTarget: (dropLocation: DragAndDropQuizQuestion.DropLocation, isCurrentDropTarget: Boolean) -> Unit,
        val onDragRelease: (originalDropLocation: DragAndDropQuizQuestion.DropLocation) -> Unit
    ) : DragAndDropAreaType
}

/**
 * Load and display the background image. Renders the drop locations onto the image and reports
 * drag events about drag items already placed in the drop locations.
 */
@Composable
internal fun DragAndDropWorkArea(
    modifier: Modifier,
    questionId: Long,
    dropLocationMapping: Map<DragAndDropQuizQuestion.DropLocation, DragAndDropQuizQuestion.DragItem>,
    imageUrl: String,
    dropLocations: List<DragAndDropQuizQuestion.DropLocation>,
    serverUrl: String,
    authToken: String,
    type: DragAndDropAreaType
) {
    val context = LocalContext.current

    val imageProvider = BaseImageProviderImpl()
    val request = remember(imageUrl, questionId) {
        imageProvider.createImageRequest(
            context,
            imageUrl,
            serverUrl,
            authToken,
            "QQ_$questionId"
        )
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
        val localDensity = LocalDensity.current

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val maxWidthInPx = with(localDensity) { maxWidth.toPx() }

            var composableSize by remember {
                mutableStateOf(with(localDensity) {
                    IntSize(
                        maxWidth.toPx().toInt(),
                        maxHeight.toPx().toInt()
                    )
                })
            }

            val painter = remember(loadedDrawable, maxWidthInPx) {
                val bitmap = if (loadedDrawable.width < maxWidthInPx) {
                    val scale = maxWidthInPx / loadedDrawable.width.toFloat()
                    val newWidth = (loadedDrawable.width * scale).toInt()
                    val newHeight = (loadedDrawable.height * scale).toInt()

                    loadedDrawable
                        .toBitmap()
                        .copy(Bitmap.Config.ARGB_8888, true)
                        .scale(newWidth, newHeight)
                        .asImageBitmap()
                } else {
                    loadedDrawable
                        .toBitmap()
                        .asImageBitmap()
                }

                BitmapPainter(bitmap)
            }

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

            val srcSize = painter.intrinsicSize

            val dstSize = composableSize.toSize()

            val scaleFactor = ContentScale.Fit.computeScaleFactor(srcSize, dstSize)
            val actImageSize = srcSize * scaleFactor

            val (imageWidth: Float, imageHeight: Float) = actImageSize

            dropLocations.forEach { dropLocation ->
                val dragItem: DragAndDropQuizQuestion.DragItem? = dropLocationMapping[dropLocation]

                with(LocalDensity.current) {
                    val xPos = imageWidth * (dropLocation.posX?.toFloat() ?: 0f) / 2f / 100f
                    val yPos = imageHeight * (dropLocation.posY?.toFloat() ?: 0f) / 2f / 100f
                    val width = imageWidth * (dropLocation.width ?: 0.0).toFloat() / 2f / 100f
                    val height = imageHeight * (dropLocation.height ?: 0.0).toFloat() / 2f / 100f

                    val composableWidth = composableSize.width.toDp()
                    val composableHeight = composableSize.height.toDp()

                    WorkAreaDropLocation(
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
                                WorkAreaDropLocationType.Editable(
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
                                    type.sampleSolutionMappings[dropLocation] == dropLocationMapping[dropLocation]
                                WorkAreaDropLocationType.ViewOnly(
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
