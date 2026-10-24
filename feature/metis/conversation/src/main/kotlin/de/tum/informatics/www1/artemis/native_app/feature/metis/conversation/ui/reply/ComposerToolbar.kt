package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.autocomplete.AutoCompleteType
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.autocomplete.LocalReplyAutoCompleteHintProvider

@Composable
internal fun ComposerToolbar(
    textFieldValue: TextFieldValue,
    typingMarks: Set<MarkdownStyle>? = null,
    formattingExpanded: Boolean,
    isEmojiPickerEnabled: Boolean,
    onToggleFormatting: () -> Unit,
    onApplyStyle: (MarkdownStyle) -> Unit,
    onLink: () -> Unit,
    onOpenImagePicker: () -> Unit,
    onOpenFilePicker: () -> Unit,
    onOpenEmojiPicker: () -> Unit,
    onMention: ((AutoCompleteType) -> Unit)?,
    capabilities: ComposerEditingCapabilities,
    canUndo: Boolean = false,
    canRedo: Boolean = false,
    onUndo: () -> Unit = {},
    onRedo: () -> Unit = {},
    trailingContent: @Composable RowScope.() -> Unit,
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (formattingExpanded) {
            ComposerAction(stringResource(R.string.reply_hide_formatting), onToggleFormatting, selected = true) {
                Icon(Icons.Default.Close, contentDescription = null)
            }
            FormattingOptions(
                modifier = Modifier.weight(1f),
                value = textFieldValue,
                typingMarks = typingMarks,
                capabilities = capabilities,
                applyMarkdownStyle = onApplyStyle,
                onLink = onLink,
                canUndo = canUndo, canRedo = canRedo, onUndo = onUndo, onRedo = onRedo,
            )
        } else {
            AttachmentAction(onOpenImagePicker, onOpenFilePicker, onMention, capabilities.inlineActions)
            // Quick actions can scroll on very narrow split-screen windows; Send never scrolls.
            Row(Modifier.weight(1f).horizontalScroll(androidx.compose.foundation.rememberScrollState())) {
                ComposerAction(stringResource(R.string.reply_show_formatting), onToggleFormatting) {
                    Text("Aa", style = MaterialTheme.typography.titleMedium)
                }
                if (isEmojiPickerEnabled) {
                    ComposerAction(stringResource(R.string.reply_add_emoji), onOpenEmojiPicker, enabled = capabilities.inlineActions) {
                        Icon(Icons.Outlined.EmojiEmotions, contentDescription = null)
                    }
                }
                if (onMention != null) {
                    ComposerAction(stringResource(R.string.reply_mention_member), { onMention(AutoCompleteType.USERS) }, enabled = capabilities.inlineActions) {
                        Icon(Icons.Default.AlternateEmail, contentDescription = null)
                    }
                    ComposerAction(stringResource(R.string.reply_reference), { onMention(AutoCompleteType.CHANNELS) }, enabled = capabilities.inlineActions) {
                        Icon(Icons.Default.Numbers, contentDescription = null)
                    }
                }
            }
        }
        trailingContent()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ComposerAction(
    label: String,
    onClick: () -> Unit,
    selected: Boolean? = null,
    enabled: Boolean = true,
    tonal: Boolean = false,
    content: @Composable () -> Unit,
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { PlainTooltip { Text(label) } },
        state = rememberTooltipState(),
    ) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.size(48.dp).semantics {
                contentDescription = label
                if (selected != null) this.selected = selected
            },
            shape = if (tonal) CircleShape else RoundedCornerShape(12.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = when { selected == true -> MaterialTheme.colorScheme.primary.copy(alpha = 0.14f); tonal -> MaterialTheme.colorScheme.surfaceContainerHighest; else -> Color.Transparent },
                contentColor = if (selected == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            content = content,
        )
    }
}

@Composable
private fun AttachmentAction(onImage: () -> Unit, onFile: () -> Unit, onReference: ((AutoCompleteType) -> Unit)?, enabled: Boolean) {
    var expanded by remember { mutableStateOf(false) }
    val faqEnabled = LocalReplyAutoCompleteHintProvider.current.isFaqEnabled
    Box {
        ComposerAction(stringResource(R.string.reply_attach), { expanded = true }, tonal = true, enabled = enabled) {
            Icon(Icons.Default.Add, contentDescription = null)
        }
        DropdownMenu(expanded, onDismissRequest = { expanded = false }, properties = PopupProperties(focusable = false)) {
            ActionMenuItem(R.string.reply_format_image_upload, Icons.Default.Image) { expanded = false; onImage() }
            ActionMenuItem(R.string.reply_format_file_upload, Icons.Default.AttachFile) { expanded = false; onFile() }
            if (onReference != null) {
                HorizontalDivider()
                ActionMenuItem(R.string.reply_format_mention_exercises, Icons.AutoMirrored.Filled.ListAlt) { expanded = false; onReference(AutoCompleteType.EXERCISES) }
                ActionMenuItem(R.string.reply_format_mention_lectures, Icons.Default.School) { expanded = false; onReference(AutoCompleteType.LECTURES) }
                if (faqEnabled) {
                    ActionMenuItem(R.string.reply_format_mention_faqs, Icons.Default.QuestionMark) { expanded = false; onReference(AutoCompleteType.FAQS) }
                }
            }
        }
    }
}

@Composable
private fun ActionMenuItem(label: Int, icon: ImageVector, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(stringResource(label)) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        onClick = onClick,
    )
}
