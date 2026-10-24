package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.MarkdownEditEngine
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.MarkdownDocument

@Composable
internal fun FormattingOptions(
    modifier: Modifier,
    value: TextFieldValue,
    applyMarkdownStyle: (MarkdownStyle) -> Unit,
    onLink: () -> Unit,
    typingMarks: Set<MarkdownStyle>? = null,
    capabilities: ComposerEditingCapabilities,
    canUndo: Boolean = false,
    canRedo: Boolean = false,
    onUndo: () -> Unit = {},
    onRedo: () -> Unit = {},
) {
    val active = remember(value.text, value.selection) { MarkdownEditEngine.activeMarks(value) }
    val controls = listOf(
        Triple(MarkdownStyle.Bold, Icons.Default.FormatBold, R.string.reply_format_bold),
        Triple(MarkdownStyle.Italic, Icons.Default.FormatItalic, R.string.reply_format_italic),
        Triple(MarkdownStyle.Underline, Icons.Default.FormatUnderlined, R.string.reply_format_underline),
        Triple(MarkdownStyle.Strikethrough, Icons.Default.FormatStrikethrough, R.string.reply_format_strikethrough),
        Triple(null, Icons.Default.Link, R.string.reply_format_link),
        Triple(MarkdownStyle.OrderedList, Icons.Default.FormatListNumbered, R.string.reply_format_ordered),
        Triple(MarkdownStyle.UnorderedList, Icons.AutoMirrored.Filled.FormatListBulleted, R.string.reply_format_unordered),
        Triple(MarkdownStyle.Blockquote, Icons.Default.FormatQuote, R.string.reply_format_quote),
        Triple(MarkdownStyle.InlineCode, Icons.Default.Code, R.string.reply_format_inline_code),
        Triple(MarkdownStyle.CodeBlock, Icons.Default.DataObject, R.string.reply_format_code_block),
    )
    Row(modifier.horizontalScroll(rememberScrollState())) {
        for ((style, icon, label) in controls) {
            ComposerAction(
                label = stringResource(label),
                enabled = if (style == null) capabilities.link else style == MarkdownStyle.CodeBlock || capabilities.inlineActions,
                selected = style?.let { if (typingMarks != null && value.selection.collapsed && it in MarkdownDocument.inlineStyles) it in typingMarks else it in active } ?: capabilities.linkActive,
                onClick = { if (style == null) onLink() else applyMarkdownStyle(style) },
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            }
        }
        ComposerAction(stringResource(R.string.reply_undo), onUndo, enabled = canUndo) { Icon(Icons.AutoMirrored.Filled.Undo, null) }
        ComposerAction(stringResource(R.string.reply_redo), onRedo, enabled = canRedo) { Icon(Icons.AutoMirrored.Filled.Redo, null) }
    }
}
