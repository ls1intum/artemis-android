package de.tum.informatics.www1.artemis.native_app.feature.quiz.question.draganddrop.body

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.google.accompanist.flowlayout.FlowRow
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.DragAndDropQuizQuestion

private val dragItemRowBackgroundColor: Color
    @Composable get() = if (isSystemInDarkTheme()) Color.DarkGray else Color.LightGray

internal sealed interface DragAndDropDragItemsRowType {
    object ViewOnly : DragAndDropDragItemsRowType

    data class Editable(val onDragRelease: (DragAndDropQuizQuestion.DragItem) -> Unit) :
        DragAndDropDragItemsRowType
}

@Composable
internal fun DragAndDropAvailableDragItemsContainer(
    modifier: Modifier,
    dragItems: List<DragAndDropQuizQuestion.DragItem>,
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
                        pictureFilePath = dragItem.pictureFilePath,
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
