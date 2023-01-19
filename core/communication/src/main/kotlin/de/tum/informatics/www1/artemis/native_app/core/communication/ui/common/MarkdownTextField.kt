package de.tum.informatics.www1.artemis.native_app.core.communication.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.InputChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.communication.R
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.MarkdownText

/**
 * @param sendButton composable centered vertically right to the text field.
 */
@Composable
internal fun MarkdownTextField(
    modifier: Modifier,
    text: String,
    focusRequester: FocusRequester = remember { FocusRequester() },
    sendButton: @Composable () -> Unit = {},
    onFocusLost: () -> Unit = {},
    onTextChanged: (String) -> Unit
) {
    var selectedType by remember { mutableStateOf(ViewType.TEXT) }
    var hadFocus by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Row(modifier = Modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InputChip(
                selected = selectedType == ViewType.TEXT,
                onClick = { selectedType = ViewType.TEXT },
                label = {
                    Text(text = stringResource(id = R.string.markdown_textfield_tab_text))
                }
            )

            InputChip(
                selected = selectedType == ViewType.PREVIEW,
                onClick = { selectedType = ViewType.PREVIEW },
                enabled = text.isNotEmpty(),
                label = {
                    Text(text = stringResource(id = R.string.markdown_textfield_tab_preview))
                }
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            val textModifier = Modifier.weight(1f)
            when (selectedType) {
                ViewType.TEXT -> {
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        modifier = textModifier
                            .focusRequester(focusRequester)
                            .onFocusChanged { focusState ->
                                if (focusState.hasFocus) {
                                    hadFocus = true
                                }

                                if (!focusState.hasFocus && hadFocus) {
                                    onFocusLost()
                                    hadFocus = false
                                }
                            },
                        value = text,
                        onValueChange = onTextChanged
                    )
                }

                ViewType.PREVIEW -> {
                    MarkdownText(
                        modifier = textModifier,
                        markdown = text
                    )
                }
            }

            sendButton()
        }
    }
}

private enum class ViewType {
    TEXT,
    PREVIEW
}