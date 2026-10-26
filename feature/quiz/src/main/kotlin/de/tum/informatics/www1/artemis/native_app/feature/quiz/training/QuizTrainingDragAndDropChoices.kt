package de.tum.informatics.www1.artemis.native_app.feature.quiz.training

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.DragAndDropQuizQuestion
import de.tum.informatics.www1.artemis.native_app.feature.quiz.R
import de.tum.informatics.www1.artemis.native_app.feature.quiz.question.draganddrop.body.DragItemUiElementContent

/** A one-use assignment uses the same item/location IDs as the visual drag interaction. */
@Composable
internal fun TrainingDragAndDropChoices(
    question: DragAndDropQuizQuestion,
    state: TrainingState,
    session: QuizTrainingSession,
) {
    if (state.stage != TrainingStage.ANSWERING) return
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(R.string.training_assign_without_drag),
            style = MaterialTheme.typography.titleMedium,
        )
        question.dropLocations.forEachIndexed { index, location ->
            key(location.id) {
                var expanded by remember { mutableStateOf(false) }
                val selected = question.dragItemById[state.draft.mappings[location.id]]
                val selectedLabel =
                    selected?.text?.takeIf { it.isNotBlank() }
                        ?: selected?.let {
                            stringResource(
                                R.string.training_image_item,
                                question.dragItems.indexOf(it) + 1,
                            )
                        }
                        ?: stringResource(R.string.training_empty_target)
                Text(
                    stringResource(
                        R.string.training_target_position,
                        (location.posX ?: 0.0) / 2,
                        (location.posY ?: 0.0) / 2,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
                Box {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            stringResource(
                                R.string.training_target_selection,
                                index + 1,
                                selectedLabel,
                            )
                        )
                    }
                    DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.training_leave_empty)) },
                            onClick = {
                                expanded = false
                                session.clearDrop(location.id)
                            },
                        )
                        question.dragItems.forEachIndexed { itemIndex, item ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            item.text?.takeIf { it.isNotBlank() }
                                                ?: stringResource(
                                                    R.string.training_image_item,
                                                    itemIndex + 1,
                                                )
                                        )
                                        item.pictureFilePath?.let { path ->
                                            DragItemUiElementContent(
                                                Modifier.padding(top = 4.dp),
                                                null,
                                                path,
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    expanded = false
                                    session.moveItem(item.id, location.id)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
