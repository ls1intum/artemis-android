package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.MarkdownStyle

/** Native Enter behavior: continue a block, or leave it from an empty item. */
object MarkdownListContinuationUtil {
    fun continueListIfApplicable(oldText: String, newValue: TextFieldValue): TextFieldValue {
        val text = newValue.text
        val cursor = newValue.selection.start
        if (!newValue.selection.collapsed || text.length != oldText.length + 1 || cursor == 0 || text[cursor - 1] != '\n') return newValue
        val oldCursor = cursor - 1
        val inCode = MarkdownPresentation.parse(oldText).spans.any {
            it.style in setOf(MarkdownStyle.CodeBlock, MarkdownStyle.InlineCode) && oldCursor in it.contentStart..it.contentEnd
        }
        if (inCode) return newValue
        val start = text.lastIndexOf('\n', cursor - 2) + 1
        val previousLine = text.substring(start, cursor - 1)
        val match = Regex("^(\\s*)([-+*•] |(\\d+)\\. |[>│] )(.*)$").matchEntire(previousLine) ?: return newValue
        val indent = match.groupValues[1]
        val marker = match.groupValues[2]
        if (match.groupValues[4].isBlank()) {
            return TextFieldValue(text.removeRange(start, cursor - 1), TextRange(start + 1))
        }
        val number = match.groupValues[3].toLongOrNull()
        val prefix = indent + if (number != null) "${number + 1}. " else marker
        return newValue.copy(text = text.substring(0, cursor) + prefix + text.substring(cursor), selection = TextRange(cursor + prefix.length))
    }
}
