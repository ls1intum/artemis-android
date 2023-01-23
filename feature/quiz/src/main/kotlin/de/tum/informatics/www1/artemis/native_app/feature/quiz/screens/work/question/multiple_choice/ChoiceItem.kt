package de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work.question.multiple_choice

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Help
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.feature.quiz.R

sealed interface ChoiceItemType {
    object ViewResult : ChoiceItemType

    data class Editable(
        val onRequestSelect: (isSelected: Boolean) -> Unit
    ) : ChoiceItemType
}

@Composable
internal fun ChoiceItem(
    modifier: Modifier,
    text: String,
    hasHint: Boolean,
    isSelected: Boolean,
    isSingleChoice: Boolean,
    onRequestDisplayHint: () -> Unit,
    type: ChoiceItemType
) {
    Box(
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

            if (hasHint) {
                IconButton(onClick = onRequestDisplayHint) {
                    Icon(
                        imageVector = Icons.Default.Help,
                        contentDescription = stringResource(id = R.string.quiz_participation_multiple_choice_help_button_content_description)
                    )
                }
            }

            val areButtonsEnabled = when (type) {
                is ChoiceItemType.Editable -> true
                ChoiceItemType.ViewResult -> false
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
                        ChoiceItemType.ViewResult -> {
                            {}
                        }
                    },
                    enabled = areButtonsEnabled
                )
            }
        }
    }
}