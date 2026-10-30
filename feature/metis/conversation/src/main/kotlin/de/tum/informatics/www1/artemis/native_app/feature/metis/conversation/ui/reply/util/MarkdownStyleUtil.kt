package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.MarkdownStyle

internal object MarkdownStyleUtil {

    private val blockPrefixes = mapOf(
        MarkdownStyle.Blockquote to Regex("^> ?"),
        MarkdownStyle.OrderedList to Regex("^\\d+\\. "),
        MarkdownStyle.UnorderedList to Regex("^[-+*] "),
    )

    fun isActive(style: MarkdownStyle, value: TextFieldValue): Boolean = MarkdownEditEngine.isActive(style, value)

    fun toggle(style: MarkdownStyle, value: TextFieldValue): TextFieldValue = when {
        style in blockPrefixes -> toggleLines(style, value)
        style == MarkdownStyle.CodeBlock -> MarkdownEditEngine.toggleCodeBlock(value)
        else -> MarkdownEditEngine.toggleInline(style, value)
    }

    private fun toggleLines(style: MarkdownStyle, value: TextFieldValue): TextFieldValue {
        val text = value.text
        val start = text.lastIndexOf('\n', value.selection.min - 1) + 1
        // A selection ending at the next line's start belongs to the preceding lines.
        val lastSelected = if (!value.selection.collapsed) (value.selection.max - 1).coerceAtLeast(start) else value.selection.max
        val end = text.indexOf('\n', lastSelected).let { if (it < 0) text.length else it }
        val lines = text.substring(start, end).split('\n')
        val remove = lines.all { blockPrefixes.getValue(style).containsMatchIn(it) }
        val changes = mutableListOf<Triple<Int, Int, String>>()
        var offset = start
        lines.forEachIndexed { index, line ->
            val existing = blockPrefixes.values.firstNotNullOfOrNull { it.find(line)?.value }.orEmpty()
            val prefix = if (remove) "" else if (style == MarkdownStyle.OrderedList) "${index + 1}. " else style.startTag
            changes += Triple(offset, existing.length, prefix)
            offset += line.length + 1
        }
        var result = text
        for ((at, length, prefix) in changes.asReversed()) result = result.replaceRange(at, at + length, prefix)
        fun map(position: Int) = position + changes.sumOf { (at, length, prefix) ->
            if (position >= at + length) prefix.length - length else if (position >= at) prefix.length - (position - at) else 0
        }
        return TextFieldValue(result, TextRange(map(value.selection.start), map(value.selection.end)))
    }

    fun insertLink(value: TextFieldValue, label: String, destination: String, preserveLabelMarkup: Boolean = false): TextFieldValue? {
        val url = destination.trim()
        val uri = runCatching { java.net.URI(url) }.getOrNull() ?: return null
        if (uri.scheme?.lowercase() !in setOf("https", "http", "mailto")) return null
        if (uri.scheme.lowercase() != "mailto" && uri.host.isNullOrBlank()) return null
        if (uri.scheme.lowercase() == "mailto" && !uri.schemeSpecificPart.contains('@')) return null
        if (label.isBlank() || label.contains('\n') || url.any { it.isWhitespace() }) return null
        return MarkdownEditEngine.insertLink(value, label, url, preserveLabelMarkup)
    }

    /** Rebase a dialog edit around a single external change, such as a completed upload. */
    fun rebaseEdit(original: TextFieldValue, current: TextFieldValue, edited: TextFieldValue): TextFieldValue? {
        if (original.text == current.text) return edited
        fun change(before: String, after: String): Triple<Int, Int, String> {
            val prefix = before.commonPrefixWith(after).length
            val suffix = before.substring(prefix).commonSuffixWith(after.substring(prefix)).length
            return Triple(prefix, before.length - suffix, after.substring(prefix, after.length - suffix))
        }
        val (start, end, replacement) = change(original.text, edited.text)
        val (externalStart, externalEnd, externalReplacement) = change(original.text, current.text)
        val shift = when {
            externalStart >= end -> 0
            externalEnd <= start -> externalReplacement.length - (externalEnd - externalStart)
            else -> return null // The selected text changed. Keep the dialog open and explain why.
        }
        return TextFieldValue(current.text.replaceRange(start + shift, end + shift, replacement), TextRange(start + shift + replacement.length))
    }

    /** Keep live typing styles during whitespace input; emit valid CommonMark when sending. */
    fun prepareForSend(source: String): String {
        val empty = MarkdownPresentation.parse(source).spans.filter {
            it.style in MarkdownDocument.inlineStyles && it.contentStart == it.contentEnd && source.getOrNull(it.start) == '<'
        }.sortedByDescending { it.start }
        var result = source
        var boundary = source.length
        for (span in empty) if (span.end <= boundary) {
            result = result.removeRange(span.start, span.end)
            boundary = span.start
        }
        return result
    }

    /** Backspace at an invisible delimiter removes the format rather than exposing broken syntax. */
    fun normalizeEdit(previous: TextFieldValue, next: TextFieldValue): TextFieldValue {
        if (next.text.length >= previous.text.length || !previous.selection.collapsed) return next
        val cursor = previous.selection.start
        val presentation = MarkdownPresentation.parse(previous.text, previous.selection)
        val span = presentation.spans.lastOrNull { cursor == it.contentStart && it.style != null }
        if (span != null && cursor > 0 && previous.text.removeRange(cursor - 1, cursor) == next.text) {
            val content = previous.text.substring(span.contentStart, span.contentEnd)
            return TextFieldValue(previous.text.replaceRange(span.start, span.end, content), TextRange(span.start))
        }
        if (cursor > 0 && previous.text.removeRange(cursor - 1, cursor) == next.text) {
            val mapping = presentation.offsetMapping
            if (mapping.originalToTransformed(cursor) == mapping.originalToTransformed(cursor - 1)) {
                val atom = presentation.spans.firstOrNull { it.destination?.startsWith("artemis:") == true && cursor == it.end }
                if (atom != null) return TextFieldValue(previous.text.removeRange(atom.start, atom.end), TextRange(atom.start))
                var character = cursor - 1
                while (character > 0 && mapping.originalToTransformed(character) == mapping.originalToTransformed(character + 1)) character--
                if (mapping.originalToTransformed(character) != mapping.originalToTransformed(character + 1)) {
                    val start = if (previous.text[character].isLowSurrogate() && character > 0 && previous.text[character - 1].isHighSurrogate()) character - 1 else character
                    return TextFieldValue(previous.text.removeRange(start, character + 1), TextRange(start))
                }
            }
        }
        return next
    }

    fun apply(
        style: MarkdownStyle,
        currentTextFieldValue: TextFieldValue
    ): TextFieldValue {
        val selection = currentTextFieldValue.selection
        val text = currentTextFieldValue.text

        val startTag = style.startTag
        val endTag = style.endTag

        if (selection.collapsed) {
            // No text selected
            return applyAtCursorPosition(
                style = style,
                text = text,
                cursorIndex = selection.start,
                startTag = startTag,
                endTag = endTag
            )
        } else {
            return applyOnSelectedTextRange(
                text = text,
                selection = selection,
                style = style,
                startTag = startTag,
                endTag = endTag
            )
        }
    }

    private fun applyAtCursorPosition(
        style: MarkdownStyle,
        text: String,
        cursorIndex: Int,
        startTag: String,
        endTag: String
    ): TextFieldValue {
        if (style == MarkdownStyle.CodeBlock) {
            // Insert code block with newlines
            val newText = text.substring(0, cursorIndex) +
                    "$startTag\n\n$endTag" +
                    text.substring(cursorIndex)
            val newCursorPosition = cursorIndex + startTag.length + 1

            return TextFieldValue(
                text = newText,
                selection = TextRange(newCursorPosition, newCursorPosition)
            )
        } else {
            // Other styles
            val newText = text.substring(
                0,
                cursorIndex
            ) + startTag + endTag + text.substring(cursorIndex)
            val newCursorPosition = cursorIndex + startTag.length

            return TextFieldValue(
                text = newText,
                selection = TextRange(newCursorPosition, newCursorPosition)
            )
        }
    }

    private fun applyOnSelectedTextRange(
        text: String,
        selection: TextRange,
        style: MarkdownStyle,
        startTag: String,
        endTag: String
    ): TextFieldValue {
        val selectedText = text.substring(selection.min, selection.max)
        if (style == MarkdownStyle.CodeBlock) {
            val newText = text.substring(0, selection.min) +
                    "$startTag\n$selectedText\n$endTag" +
                    text.substring(selection.max)
            val newSelection = TextRange(
                selection.min + startTag.length + 1,
                selection.max + startTag.length + 1
            )

            return TextFieldValue(
                text = newText,
                selection = newSelection
            )
        } else {
            val newText = text.substring(0, selection.min) +
                    startTag +
                    selectedText +
                    endTag +
                    text.substring(selection.max)
            val newSelection = TextRange(selection.max + startTag.length + endTag.length)

            return TextFieldValue(
                text = newText,
                selection = newSelection
            )
        }
    }

}
