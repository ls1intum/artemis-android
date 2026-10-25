package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.MarkdownStyle

/** Rebuild only the affected inline span, preserving independent styles and atomic payloads. */
internal object InlineSelectionFormatting {
    private val tags = listOf(
        MarkdownStyle.Bold to "strong",
        MarkdownStyle.Italic to "em",
        MarkdownStyle.Underline to "ins",
        MarkdownStyle.Strikethrough to "s",
    )

    private data class Piece(val raw: String, val start: Int, val end: Int, val styles: Set<MarkdownStyle>, val isReference: Boolean)

    fun remove(style: MarkdownStyle, value: TextFieldValue, span: MarkdownPresentation.Span): TextFieldValue {
        val source = value.text.substring(span.start, span.end)
        val presentation = MarkdownPresentation.parse(source)
        val mapping = presentation.offsetMapping
        val from = mapping.originalToTransformed(value.selection.min - span.start)
        val to = mapping.originalToTransformed(value.selection.max - span.start)
        val pieces = mutableListOf<Piece>()
        var index = 0
        while (index < presentation.text.length) {
            val sourceIndex = mapping.transformedToOriginal(index)
            val reference = presentation.spans.firstOrNull {
                it.destination != null && mapping.originalToTransformed(it.contentStart) == index
            }
            val end = reference?.let { mapping.originalToTransformed(it.contentEnd) }?.takeIf { it > index } ?: index + 1
            val marks = presentation.spans.filter {
                it.style != null && it.style != MarkdownStyle.CodeBlock && sourceIndex in it.contentStart until it.contentEnd
            }.mapNotNull { it.style }.toMutableSet()
            if (index < to && end > from) marks.remove(style)
            pieces += Piece(reference?.let { source.substring(it.start, it.end) } ?: presentation.text.substring(index, end), index, end, marks, reference != null)
            index = end
        }

        val result = StringBuilder()
        var startOffset: Int? = null
        var endOffset: Int? = null
        var pieceIndex = 0
        while (pieceIndex < pieces.size) {
            val first = pieces[pieceIndex]
            val group = mutableListOf(first)
            pieceIndex++
            while (pieceIndex < pieces.size && pieces[pieceIndex].styles == first.styles && pieces[pieceIndex].start != from && pieces[pieceIndex].start != to) {
                group += pieces[pieceIndex++]
            }
            // Closing and reopening tags at a collapsed caret leaves a plain insertion point.
            if (from == to && first.start == from) startOffset = result.length
            val activeTags = tags.filter { it.first in first.styles }.map { it.second }
            for (tag in activeTags) result.append("<$tag>")
            val code = MarkdownStyle.InlineCode in first.styles
            val body = group.map { piece ->
                if (piece.isReference || code) piece.raw else buildString {
                    for (character in piece.raw) {
                        if (character in "\\[]*_~`<>&") append('\\')
                        append(character)
                    }
                }
            }
            val codeBody = body.joinToString("")
            val codeDelimiter = if (code) "`".repeat((Regex("`+").findAll(codeBody).maxOfOrNull { it.value.length } ?: 0) + 1) else ""
            val padding = if (code && (codeBody.startsWith('`') || codeBody.endsWith('`'))) " " else ""
            result.append(codeDelimiter).append(padding)
            for ((part, encoded) in group.zip(body)) {
                if (from != to && from in part.start until part.end && startOffset == null) startOffset = result.length
                result.append(encoded)
                if (from != to && to > part.start && to <= part.end && endOffset == null) endOffset = result.length
            }
            result.append(padding).append(codeDelimiter)
            for (tag in activeTags.asReversed()) result.append("</$tag>")
        }
        val start = span.start + (startOffset ?: result.length)
        val end = if (value.selection.collapsed) start else span.start + (endOffset ?: result.length)
        return TextFieldValue(
            value.text.replaceRange(span.start, span.end, result.toString()),
            if (value.selection.reversed) TextRange(end, start) else TextRange(start, end),
        )
    }
}
