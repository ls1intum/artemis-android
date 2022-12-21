package de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.screens.work.question

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.flowlayout.FlowRow
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.DragAndDropQuizQuestion
import io.ktor.http.HttpHeaders

@Composable
internal fun DragAndDropQuizQuestionUi(
    modifier: Modifier,
    questionIndex: Int,
    question: DragAndDropQuizQuestion,
    serverUrl: String,
    authToken: String,
    onRequestDisplayHint: () -> Unit
) {
    Column(modifier = modifier) {
        QuizQuestionHeader(
            modifier = Modifier.fillMaxWidth(),
            questionIndex = questionIndex,
            title = question.title.orEmpty(),
            hasHint = question.hint != null,
            onRequestDisplayHint = onRequestDisplayHint
        )

        DragAndDropDragItemsRow(
            modifier = Modifier.fillMaxWidth(),
            dragItems = question.dragItems,
            authToken = authToken,
            serverUrl = serverUrl
        )

        val backgroundFilePath = question.backgroundFilePath
        if (backgroundFilePath != null) {
            DragAndDropArea(
                modifier = Modifier.fillMaxWidth(),
                imageUrl = serverUrl + backgroundFilePath,
                dropLocations = question.dropLocations,
                authToken = authToken
            )
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
    dragItems: List<DragAndDropQuizQuestion.DragItem>,
    serverUrl: String,
    authToken: String
) {
    Box(modifier = modifier.background(dragItemRowBackgroundColor)) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            mainAxisSpacing = 8.dp,
            crossAxisSpacing = 8.dp
        ) {
            dragItems.forEach { dragItem ->
                DragItem(
                    modifier = Modifier,
                    text = dragItem.text,
                    pictureFilePath = dragItem.pictureFilePath?.let { serverUrl + it },
                    authToken = authToken
                )
            }
        }
    }
}


@Composable
private fun DragItem(
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
    imageUrl: String,
    dropLocations: List<DragAndDropQuizQuestion.DropLocation>,
    authToken: String
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

        ImageLoader(context)
            .execute(request)
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

            val xRatio = imageWidth / currentDrawable.intrinsicWidth.toFloat()
            val yRatio = imageHeight / currentDrawable.intrinsicHeight.toFloat()

            dropLocations.forEach { dropLocation ->
                with(LocalDensity.current) {
                    val xPos = imageWidth * (dropLocation.posX?.toFloat() ?: 0f) / 2f
                    val yPos = imageHeight * (dropLocation.posY?.toFloat() ?: 0f) / 2f
                    val width = imageWidth * (dropLocation.width ?: 0.0).toFloat() / 2f
                    val height = imageHeight * (dropLocation.height ?: 0.0).toFloat() / 2f

                    ImageDropLocation(
                        modifier = Modifier
                            .absoluteOffset(
                                x = xPos.toDp(),
                                y = yPos.toDp()
                            )
                            .size(
                                width = width.toDp(),
                                height = height.toDp()
                            )
                    )
                }

            }
        }
    }
}

@Composable
private fun ImageDropLocation(modifier: Modifier) {
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

    Canvas(modifier = modifier) {
        drawRect(Color.White)

        drawRect(
            color = outlineColor, style = stroke
        )
    }
}