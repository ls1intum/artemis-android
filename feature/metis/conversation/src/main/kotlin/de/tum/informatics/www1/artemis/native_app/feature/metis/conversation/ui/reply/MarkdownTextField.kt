package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.MarkdownText
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R


const val TEST_TAG_MARKDOWN_TEXTFIELD = "TEST_TAG_MARKDOWN_TEXTFIELD"

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
    onFileSelect: (Uri?, String) -> Unit = { _, _ -> },
    onImageSelect: (Uri?,String) -> Unit = {_, _ ->},
    onFocusAcquired: () -> Unit = {},
    onFocusLost: () -> Unit = {},
    onTextChanged: (TextFieldValue) -> Unit
) {
    val text = textFieldValue.text
    val context = LocalContext.current

    var selectedType by remember { mutableStateOf(ViewType.TEXT) }
    var hadFocus by remember { mutableStateOf(false) }

    // Launchers for file and image selection
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        onImageSelect(uri, resolveFileName(context, uri))
    }

    val filePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        onFileSelect(uri, resolveFileName(context, uri))
    }

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

            Spacer(modifier = Modifier.weight(1f))

            InputChip(
                selected = true,
                onClick = { filePickerLauncher.launch("*/*") },
                enabled = true,
                label = {
                    Icon(
                        Icons.Default.AttachFile,
                        contentDescription = "Attach File"
                    )
                }
            )

            InputChip(
                selected = true,
                onClick = { imagePickerLauncher.launch("image/*") },
                enabled = true,
                label = {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = "Select Image"
                    )
                }
            )

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
                            }
                            .testTag(TEST_TAG_MARKDOWN_TEXTFIELD),
                        value = textFieldValue,
                        onValueChange = onTextChanged,
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

private fun resolveFileName(context: Context, uri: Uri?): String {
    return try {
        val resolver: ContentResolver = context.contentResolver
        uri?.let {
            resolver.query(it, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(nameIndex)
            }
        } ?: uri?.lastPathSegment.orEmpty()
    } catch (e: Exception) {
        Log.e("MarkdownTextField", "Error resolving file name", e)
        uri?.lastPathSegment.orEmpty()
    }
}
