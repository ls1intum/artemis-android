package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.MarkdownPresentation
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.MarkdownStyleUtil

@Composable
internal fun ComposerLinkDialog(value: TextFieldValue, currentValue: TextFieldValue, onDismiss: () -> Unit, onConfirm: (TextFieldValue) -> Unit) {
    val existing = remember(value) {
        MarkdownPresentation.parse(value.text).spans.firstOrNull {
            it.kind == MarkdownPresentation.Kind.LINK && value.selection.min >= it.contentStart && value.selection.max <= it.contentEnd
        }
    }
    val selection = existing?.let { value.copy(selection = TextRange(it.start, it.end)) } ?: value
    val originalLabel = existing?.let { value.text.substring(it.contentStart, it.contentEnd) }
    val initialLabel = MarkdownPresentation.parse(originalLabel ?: value.text.substring(value.selection.min, value.selection.max)).text
    var label by remember { mutableStateOf(initialLabel) }
    var destination by remember { mutableStateOf(existing?.destination?.replace("\\(", "(")?.replace("\\)", ")").orEmpty()) }
    val result = MarkdownStyleUtil.insertLink(selection, if (label == initialLabel && originalLabel != null) originalLabel else label,
        destination, preserveLabelMarkup = label == initialLabel && originalLabel != null)
    val rebased = result?.let { MarkdownStyleUtil.rebaseEdit(value, currentValue, it) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.reply_format_link)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (result != null && rebased == null) Text(stringResource(R.string.reply_link_draft_changed))
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text(stringResource(R.string.reply_link_text)) },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = destination,
                    onValueChange = { destination = it },
                    label = { Text(stringResource(R.string.reply_link_url)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    singleLine = true,
                    isError = destination.isNotBlank() && result == null,
                    supportingText = { if (destination.isNotBlank() && result == null) Text(stringResource(R.string.reply_link_invalid)) },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { rebased?.let(onConfirm) }, enabled = rebased != null) {
                Text(stringResource(R.string.reply_link_apply))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.reply_cancel)) } },
    )
}
