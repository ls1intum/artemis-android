package de.tum.informatics.www1.artemis.native_app.feature.quiz.question.multiple_choice

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.material.colors.ExerciseColors
import de.tum.informatics.www1.artemis.native_app.feature.quiz.R
import de.tum.informatics.www1.artemis.native_app.feature.quiz.question.ExplanationText
import de.tum.informatics.www1.artemis.native_app.feature.quiz.question.HelpText

sealed interface ChoiceItemType {
    data class ViewResult(
        val explanation: String?,
        val help: String?,
        val isCorrectChoice: Boolean
    ) : ChoiceItemType

    data class Editable(
        val hasHelp: Boolean,
        val onRequestSelect: (isSelected: Boolean) -> Unit
    ) : ChoiceItemType
}

@Composable
internal fun ChoiceItem(
    modifier: Modifier,
    text: String,
    isSelected: Boolean,
    isSingleChoice: Boolean,
    onRequestDisplayHint: () -> Unit,
    type: ChoiceItemType
) {
    Column(
        modifier = modifier
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline)
            .let {
                if (type is ChoiceItemType.Editable) {
                    it.clickable(
                        onClick = {
                            val newIsSelected = if (isSingleChoice) true else !isSelected
                            type.onRequestSelect(newIsSelected)
                        }
                    )
                } else it
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = text,
                style = MaterialTheme.typography.bodyMedium
            )

            when (type) {
                is ChoiceItemType.Editable -> {
                    if (type.hasHelp) {
                        IconButton(onClick = onRequestDisplayHint) {
                            Icon(
                                imageVector = Icons.Default.Help,
                                contentDescription = stringResource(id = R.string.quiz_participation_multiple_choice_help_button_content_description)
                            )
                        }
                    }
                }
                is ChoiceItemType.ViewResult -> {
                    val userIncorrectChoice = type.isCorrectChoice != isSelected

                    val correctStatusText =
                        stringResource(
                            id = if (type.isCorrectChoice) R.string.quiz_result_mc_correct
                            else R.string.quiz_result_mc_wrong
                        )

                    Text(
                        text = correctStatusText,
                        color = if (type.isCorrectChoice) ExerciseColors.Result.success else ExerciseColors.Result.bad,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    val iconModifier = Modifier.size(20.dp)
                    if (userIncorrectChoice) {
                        Icon(
                            modifier = iconModifier,
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = ExerciseColors.Result.medium
                        )
                    } else {
                        Box(modifier = iconModifier)
                    }
                }
            }

            val areButtonsEnabled = when (type) {
                is ChoiceItemType.Editable -> true
                is ChoiceItemType.ViewResult -> false
            }

            if (isSingleChoice) {
                RadioButton(
                    selected = isSelected,
                    onClick = {
                        if (type is ChoiceItemType.Editable) {
                            type.onRequestSelect(true)
                        }
                    },
                    enabled = areButtonsEnabled
                )
            } else {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = when (type) {
                        is ChoiceItemType.Editable -> type.onRequestSelect
                        is ChoiceItemType.ViewResult -> {
                            {}
                        }
                    },
                    enabled = areButtonsEnabled
                )
            }
        }

        if (type is ChoiceItemType.ViewResult && (type.explanation != null || type.help != null)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 8.dp)
            ) {
                type.help?.let { help ->
                    HelpText(
                        modifier = Modifier.fillMaxWidth(),
                        help = help,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                type.explanation?.let { explanation ->
                    ExplanationText(
                        modifier = Modifier.fillMaxWidth(),
                        explanation = explanation,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
