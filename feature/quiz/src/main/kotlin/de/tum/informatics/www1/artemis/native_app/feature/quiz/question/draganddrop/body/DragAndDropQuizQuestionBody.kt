package de.tum.informatics.www1.artemis.native_app.feature.quiz.question.draganddrop.body

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.DragAndDropQuizQuestion
import de.tum.informatics.www1.artemis.native_app.feature.quiz.R
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizQuestionData
import de.tum.informatics.www1.artemis.native_app.feature.quiz.question.QuizQuestionInstructionText
import de.tum.informatics.www1.artemis.native_app.feature.quiz.question.draganddrop.DragInfo
import de.tum.informatics.www1.artemis.native_app.feature.quiz.question.draganddrop.DragTargetInfo
import de.tum.informatics.www1.artemis.native_app.feature.quiz.question.draganddrop.DropTarget
import de.tum.informatics.www1.artemis.native_app.feature.quiz.question.draganddrop.LocalDragTargetInfo
import de.tum.informatics.www1.artemis.native_app.feature.quiz.question.draganddrop.body.work_area.DragAndDropAreaType
import de.tum.informatics.www1.artemis.native_app.feature.quiz.question.draganddrop.body.work_area.DragAndDropWorkArea
import de.tum.informatics.www1.artemis.native_app.feature.quiz.question.draganddrop.dragOffset
import de.tum.informatics.www1.artemis.native_app.feature.quiz.question.draganddrop.dragPosition

@Composable
internal fun DragAndDropQuizQuestionBody(
    question: DragAndDropQuizQuestion,
    data: QuizQuestionData.DragAndDropData,
) {
    var isSampleSolutionDisplayed by rememberSaveable { mutableStateOf(false) }

    val currentDragTargetInfo = remember { DragInfo() }
    val dragOffset = currentDragTargetInfo.currentDragTargetInfo.dragOffset
    val dragPosition = currentDragTargetInfo.currentDragTargetInfo.dragPosition

    var currentDropTarget: DropTarget by remember { mutableStateOf(DropTarget.Nothing) }

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

            DragAndDropAvailableDragItemsContainer(
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

            // Correct mappings as sent from the server. Not relevant for participation mode.
            val sampleSolutionMappings = remember(question.correctMappings) {
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
                sampleSolutionMappings
            } else data.dropLocationMapping

            DragAndDropWorkArea(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(if (isDraggingFromArea) 20f else 1f),
                dropLocationMapping = dropLocationMapping,
                imageUrl = backgroundFilePath,
                dropLocations = question.dropLocations,
                type = data.getDragAndDropAreaType(
                    currentDropTarget = currentDropTarget,
                    isSampleSolutionDisplayed = isSampleSolutionDisplayed,
                    sampleSolutionMappings = sampleSolutionMappings,
                    onUpdateCurrentDropTarget = { currentDropTarget = it }
                )
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

private fun QuizQuestionData.DragAndDropData.getDragAndDropAreaType(
    currentDropTarget: DropTarget,
    isSampleSolutionDisplayed: Boolean,
    sampleSolutionMappings: Map<DragAndDropQuizQuestion.DropLocation, DragAndDropQuizQuestion.DragItem>,
    onUpdateCurrentDropTarget: (DropTarget) -> Unit
): DragAndDropAreaType {
    return when (this) {
        is QuizQuestionData.DragAndDropData.Editable -> {
            DragAndDropAreaType.Editable(
                onUpdateIsCurrentDropTarget = { dropLocation, isCurrentDropTarget ->
                    // Set drop location to nothing if it is currently set to this drop location target.
                    // If it is the current drop target, set it.
                    if (
                        !isCurrentDropTarget && currentDropTarget is DropTarget.DropLocationTarget
                        && currentDropTarget.dropLocationTarget == dropLocation
                    ) {
                        onUpdateCurrentDropTarget(DropTarget.Nothing)
                    } else if (isCurrentDropTarget) {
                        onUpdateCurrentDropTarget(DropTarget.DropLocationTarget(dropLocation))
                    }
                },
                onDragRelease = { originalDropLocation ->
                    when (currentDropTarget) {
                        is DropTarget.DropLocationTarget -> {
                            // trigger swap
                            onSwapDropLocations(
                                originalDropLocation.id,
                                currentDropTarget.dropLocationTarget.id
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
        is QuizQuestionData.DragAndDropData.Result -> DragAndDropAreaType.ViewOnly(
            isSampleSolution = isSampleSolutionDisplayed,
            sampleSolutionMappings = sampleSolutionMappings
        )
    }
}

internal val dragItemOutlineColor: Color
    @Composable get() = Color.DarkGray
