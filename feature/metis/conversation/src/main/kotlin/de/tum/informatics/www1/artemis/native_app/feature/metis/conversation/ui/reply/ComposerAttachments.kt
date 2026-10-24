package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.ComposerAttachment
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.MarkdownAttachments

/** Source remains authoritative; every action is validated against the current draft. */
@Composable
internal fun ComposerAttachments(
    value: TextFieldValue,
    onTextChanged: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
) {
    val attachments = remember(value.text) { MarkdownAttachments.list(value.text) }
    val currentValue by rememberUpdatedState(value)
    val currentOnTextChanged by rememberUpdatedState(onTextChanged)
    var renaming by remember { mutableStateOf<ComposerAttachment?>(null) }
    var name by remember { mutableStateOf("") }

    if (attachments.isNotEmpty()) {
        LazyRow(
            modifier = modifier.fillMaxWidth().heightIn(max = 96.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(attachments, key = { it.start }) { attachment ->
                ComposerAttachmentTile(
                    attachment = attachment,
                    onRename = { name = attachment.name; renaming = attachment },
                    onRemove = { MarkdownAttachments.remove(currentValue, attachment)?.let(currentOnTextChanged) },
                )
            }
        }
    }

    renaming?.let { attachment ->
        val stale = attachment !in attachments
        val replacement = remember(value, attachment, name) { MarkdownAttachments.rename(value, attachment, name) }
        AlertDialog(
            onDismissRequest = { renaming = null },
            title = { Text(stringResource(R.string.reply_attachment_rename_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (stale) Text(stringResource(R.string.reply_attachment_changed))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(R.string.reply_attachment_name)) },
                        singleLine = true,
                        isError = !stale && name.isNotBlank() && replacement == null,
                        supportingText = {
                            if (!stale && name.isNotBlank() && replacement == null) {
                                Text(stringResource(R.string.reply_attachment_invalid_name))
                            }
                        },
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        MarkdownAttachments.rename(currentValue, attachment, name)?.let {
                            currentOnTextChanged(it)
                            renaming = null
                        }
                    },
                    enabled = replacement != null,
                    modifier = Modifier.heightIn(min = 48.dp),
                ) { Text(stringResource(R.string.reply_attachment_rename)) }
            },
            dismissButton = {
                TextButton(onClick = { renaming = null }, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(stringResource(R.string.reply_attachment_cancel))
                }
            },
        )
    }
}

@Composable
private fun ComposerAttachmentTile(attachment: ComposerAttachment, onRename: () -> Unit, onRemove: () -> Unit) {
    var menuExpanded by remember(attachment) { mutableStateOf(false) }
    val description = stringResource(
        if (attachment.isImage) R.string.reply_attachment_manage_image else R.string.reply_attachment_manage_file,
        attachment.name,
    )
    Box {
        Surface(
            onClick = { menuExpanded = true },
            modifier = Modifier.width(184.dp).heightIn(min = 64.dp).semantics(mergeDescendants = true) {
                contentDescription = description
            },
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // The draft may contain arbitrary restored URLs. Metadata previews never fetch them.
                Icon(
                    imageVector = if (attachment.isImage) Icons.Default.Image else Icons.Default.Description,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(attachment.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        text = stringResource(if (attachment.isImage) R.string.reply_attachment_image else R.string.reply_attachment_file),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Icon(Icons.Default.MoreVert, contentDescription = null, modifier = Modifier.size(20.dp))
            }
        }
        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.reply_attachment_rename_title)) },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                onClick = { menuExpanded = false; onRename() },
                modifier = Modifier.heightIn(min = 48.dp),
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.reply_attachment_remove), color = MaterialTheme.colorScheme.error) },
                leadingIcon = { Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                onClick = { menuExpanded = false; onRemove() },
                modifier = Modifier.heightIn(min = 48.dp),
            )
        }
    }
}
