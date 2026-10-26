package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.MarkdownDocument
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.MarkdownPresentation

internal fun composerStyledText(presentation: MarkdownPresentation, colors: ColorScheme): AnnotatedString = buildAnnotatedString {
    // Compose inserts paragraph breaks at style boundaries. Merge consecutive
    // quote lines and replace only boundary newlines with zero-width separators
    // in the visual text, avoiding a second break. UTF-16 offsets remain native;
    // the editable value, clipboard and Markdown still contain real newlines.
    val quotes = mutableListOf<TextRange>()
    for (span in presentation.spans.filter { it.style == MarkdownStyle.Blockquote }) {
        val visible = presentation.visibleRange(span)
        val end = visible.max + if (presentation.text.getOrNull(visible.max) == '\n') 1 else 0
        val previous = quotes.lastOrNull()
        if (previous != null && previous.max == visible.min && !visible.collapsed) {
            quotes[quotes.lastIndex] = TextRange(previous.min, end)
        } else quotes += TextRange(visible.min, end)
    }
    val display = StringBuilder(presentation.text)
    for ((index, range) in quotes.withIndex()) {
        if (range.min > 0 && display[range.min - 1] == '\n') display[range.min - 1] = '\u200B'
        if ((range.max < display.length || index < quotes.lastIndex) && range.max > 0 && display[range.max - 1] == '\n') {
            display[range.max - 1] = '\u200B'
        }
    }
    append(display)
    val glyphs = MarkdownDocument.glyphs(presentation)
    var index = 0
    while (index < glyphs.size) {
        val start = index
        val first = glyphs[index]
        while (index < glyphs.size && glyphs[index].marks == first.marks && glyphs[index].link == first.link && glyphs[index].atom == first.atom) index++
        val decorations = listOfNotNull(
            TextDecoration.Underline.takeIf { MarkdownStyle.Underline in first.marks || first.link != null || first.atom != null },
            TextDecoration.LineThrough.takeIf { MarkdownStyle.Strikethrough in first.marks },
        )
        val code = MarkdownStyle.InlineCode in first.marks || MarkdownStyle.CodeBlock in first.marks
        addStyle(SpanStyle(
            color = if (first.link != null || first.atom != null) colors.primary else colors.onSurface,
            fontWeight = if (MarkdownStyle.Bold in first.marks) FontWeight.Bold else FontWeight.Normal,
            fontStyle = if (MarkdownStyle.Italic in first.marks) FontStyle.Italic else FontStyle.Normal,
            fontFamily = if (code) FontFamily.Monospace else FontFamily.Default,
            background = if (MarkdownStyle.InlineCode in first.marks) colors.surfaceContainerHighest else androidx.compose.ui.graphics.Color.Transparent,
            textDecoration = TextDecoration.combine(decorations),
        ), start, index)
    }
    for (range in quotes) {
        addStyle(ParagraphStyle(textIndent = TextIndent(20.sp, 20.sp)), range.min, range.max)
    }
}

/** Draw inside the scrollable editor content, so wrapped blocks move with the native text. */
internal fun Modifier.composerBlockBackgrounds(presentation: MarkdownPresentation, styled: AnnotatedString, layout: TextLayoutResult?, colors: ColorScheme): Modifier = drawBehind {
    // The IME adds inline composition underlines after our transformation.
    // They must not invalidate block painting; text and paragraph geometry must match.
    val textLayout = layout?.takeIf {
        it.layoutInput.text.text == styled.text && it.layoutInput.text.paragraphStyles == styled.paragraphStyles
    } ?: return@drawBehind
    val blocks = presentation.spans.filter { it.style == MarkdownStyle.Blockquote || it.style == MarkdownStyle.CodeBlock }
    for (line in 0 until textLayout.lineCount) {
        val start = textLayout.getLineStart(line)
        val end = textLayout.getLineEnd(line)
        fun contains(style: MarkdownStyle) = blocks.any { span ->
            span.style == style && presentation.visibleRange(span).let { r ->
                if (r.collapsed) r.start == start else r.min < end && r.max > start
            }
        }
        val top = textLayout.getLineTop(line)
        val height = textLayout.getLineBottom(line) - top
        if (contains(MarkdownStyle.CodeBlock)) {
            drawRect(colors.surfaceContainerHighest, Offset(0f, top), Size(size.width, height))
        } else if (contains(MarkdownStyle.Blockquote)) {
            drawRect(colors.onSurfaceVariant.copy(alpha = 0.65f), Offset(2.dp.toPx(), top), Size(3.dp.toPx(), height))
        }
    }
}
