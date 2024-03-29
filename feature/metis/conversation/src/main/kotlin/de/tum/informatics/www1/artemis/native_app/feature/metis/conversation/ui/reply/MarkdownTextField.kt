package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.MarkdownText
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R

/**
 * @param sendButton composable centered vertically right to the text field.
 */
@Composable
internal fun MarkdownTextField(
    modifier: Modifier,
    textFieldValue: TextFieldValue,
    focusRequester: FocusRequester = remember { FocusRequester() },
    sendButton: @Composable () -> Unit = {},
    topRightButton: @Composable RowScope.() -> Unit = {},
    onFocusAcquired: () -> Unit = {},
    onFocusLost: () -> Unit = {},
    onTextChanged: (TextFieldValue) -> Unit
) {
    val text = textFieldValue.text

    var selectedType by remember { mutableStateOf(ViewType.TEXT) }
    var hadFocus by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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

            Box(modifier = Modifier.weight(1f))

            topRightButton()
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
                                    onFocusAcquired()
                                }

                                if (!focusState.hasFocus && hadFocus) {
                                    onFocusLost()
                                    hadFocus = false
                                }
                            },
                        value = textFieldValue,
                        onValueChange = onTextChanged,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )
                }

                ViewType.PREVIEW -> {
                    MarkdownText(
                        markdown = text,
                        modifier = textModifier
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