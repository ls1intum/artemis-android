package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.MarkdownPresentation

internal data class ComposerEditingCapabilities(val inlineActions: Boolean, val link: Boolean, val linkActive: Boolean)

internal fun composerEditingCapabilities(value: TextFieldValue): ComposerEditingCapabilities {
    val p = MarkdownPresentation.parse(value.text)
    val selection = TextRange(p.offsetMapping.originalToTransformed(value.selection.min), p.offsetMapping.originalToTransformed(value.selection.max))
    fun overlaps(span: MarkdownPresentation.Span): Boolean = p.visibleRange(span).let { range ->
        if (selection.collapsed) selection.start in range.min..range.max else selection.min < range.max && selection.max > range.min
    }
    val code = p.spans.any { it.style == MarkdownStyle.CodeBlock && overlaps(it) }
    val atom = !selection.collapsed && p.spans.any { it.isAtomic && overlaps(it) }
    val link = p.spans.any { it.kind == MarkdownPresentation.Kind.LINK && value.selection.min >= it.contentStart && value.selection.max <= it.contentEnd }
    return ComposerEditingCapabilities(!code, !code && !atom && !p.text.substring(selection.min, selection.max).contains('\n'), link)
}
