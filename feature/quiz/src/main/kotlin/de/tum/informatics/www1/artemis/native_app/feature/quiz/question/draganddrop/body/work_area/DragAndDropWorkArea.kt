package de.tum.informatics.www1.artemis.native_app.feature.quiz.question.draganddrop.body.work_area

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.times
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.core.graphics.drawable.toBitmap
import coil.request.ImageRequest
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.DragAndDropQuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.common.image.loadAsyncImageDrawable
import de.tum.informatics.www1.artemis.native_app.feature.quiz.R
import io.ktor.http.*

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
