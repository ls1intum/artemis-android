package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import java.net.URI

/** A specific source occurrence, including the exact transport markup used to identify stale actions. */
internal data class ComposerAttachment(
    val start: Int,
    val end: Int,
    val name: String,
    val path: String,
    val isImage: Boolean,
    val originalMarkup: String,
)

internal object MarkdownAttachments {
    fun list(source: String): List<ComposerAttachment> {
        val presentation = MarkdownPresentation.parse(source)
        return presentation.spans.mapNotNull { toAttachment(presentation, it) }.sortedBy { it.start }
    }

    /** Keeps the original destination bytes, including angle brackets, escapes and relative paths. */
    fun rename(current: TextFieldValue, attachment: ComposerAttachment, name: String): TextFieldValue? {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || trimmed.any { it.isISOControl() || it == '\u2028' || it == '\u2029' }) return null
        val presentation = MarkdownPresentation.parse(current.text)
        val span = presentation.spans.firstOrNull { toAttachment(presentation, it) == attachment } ?: return null
        val replacement = current.text.substring(span.start, span.contentStart) + escapeLabel(trimmed) +
            current.text.substring(span.contentEnd, span.end)
        return replace(current, attachment, replacement)
    }

    fun remove(current: TextFieldValue, attachment: ComposerAttachment): TextFieldValue? {
        if (attachment !in list(current.text)) return null
        return replace(current, attachment, "")
    }

    private fun toAttachment(presentation: MarkdownPresentation, span: MarkdownPresentation.Span): ComposerAttachment? {
        val isImage = span.kind == MarkdownPresentation.Kind.IMAGE
        if (!isImage && span.kind !in setOf(MarkdownPresentation.Kind.ATTACHMENT, MarkdownPresentation.Kind.LINK)) return null
        val path = span.destination ?: return null
        // Only the URL path identifies an upload; query parameters can contain unrelated file URLs.
        if (!isImage && runCatching { URI(path).path?.contains("/files/") == true }.getOrDefault(false).not()) return null
        val label = MarkdownPresentation.unescape(presentation.source.substring(span.contentStart, span.contentEnd))
        return ComposerAttachment(
            start = span.start,
            end = span.end,
            name = label.ifEmpty { path.substringBefore('?').substringBefore('#').substringAfterLast('/').ifEmpty { path } },
            path = path,
            isImage = isImage,
            originalMarkup = presentation.source.substring(span.start, span.end),
        )
    }

    private fun escapeLabel(name: String): String = buildString {
        for (character in name) {
            if (character in "\\`*_[]<>~|&$") append('\\')
            append(character)
        }
    }

    private fun replace(current: TextFieldValue, attachment: ComposerAttachment, replacement: String): TextFieldValue {
        val difference = replacement.length - (attachment.end - attachment.start)
        fun map(offset: Int): Int = when {
            offset <= attachment.start -> offset
            offset >= attachment.end -> offset + difference
            else -> attachment.start + replacement.length
        }
        return current.copy(
            text = current.text.replaceRange(attachment.start, attachment.end, replacement),
            selection = TextRange(map(current.selection.start), map(current.selection.end)),
            composition = null,
        )
    }
}
