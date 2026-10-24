package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import android.content.ClipData
import android.os.PersistableBundle
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.text.input.TextFieldValue
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.MarkdownDocument
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.MarkdownEditEngine
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.MarkdownPresentation
import java.util.UUID

/** Keep Android's selection menu and readable clipboard text, with a Markdown companion for Artemis. */
internal class ComposerClipboard(
    private val delegate: Clipboard,
    private val currentValue: () -> TextFieldValue,
) : Clipboard {
    private data class Paste(val source: TextFieldValue, val markdown: String, val plainText: String, val token: String, val expected: String)
    private val pendingPastes = ArrayDeque<Paste>()
    override val nativeClipboard get() = delegate.nativeClipboard

    override suspend fun setClipEntry(clipEntry: ClipEntry?) {
        if (clipEntry == null) { delegate.setClipEntry(null); return }
        val source = currentValue()
        val visible = MarkdownEditEngine.visibleValue(source)
        val plain = clipEntry.clipData.getItemAt(0).text?.toString()
        if (visible.selection.collapsed || plain != visible.text.substring(visible.selection.min, visible.selection.max)) {
            delegate.setClipEntry(clipEntry)
            return
        }
        val glyphs = MarkdownDocument.glyphs(MarkdownPresentation.parse(source.text))
        val markdown = MarkdownDocument.encode(glyphs.subList(visible.selection.min, visible.selection.max))
        val copy = ClipData.newPlainText(clipEntry.clipData.description.label, plain)
        copy.description.extras = PersistableBundle().apply { putString(MARKDOWN, markdown) }
        delegate.setClipEntry(ClipEntry(copy))
    }

    override suspend fun getClipEntry(): ClipEntry? {
        val entry = delegate.getClipEntry() ?: return null
        if (entry.clipData.itemCount != 1) return entry
        // Do not resolve foreign content URIs or import attachment objects from other apps.
        val plain = entry.clipData.getItemAt(0).text?.toString() ?: return entry
        val markdown = entry.clipData.description.extras?.getString(MARKDOWN) ?: plain
        val source = currentValue()
        val visible = MarkdownEditEngine.visibleValue(source)
        // A private insertion token distinguishes a real paste from the framework's
        // clipboard-availability probes. It never replaces the readable system clipboard.
        val token = "\uE000artemis:" + UUID.randomUUID() + "\uE001"
        val paste = Paste(source, markdown, plain, token, visible.text.replaceRange(visible.selection.min, visible.selection.max, token))
        pendingPastes.addLast(paste)
        if (pendingPastes.size > 8) pendingPastes.removeFirst()
        return ClipEntry(ClipData.newPlainText(entry.clipData.description.label, token))
    }

    fun consumePaste(source: TextFieldValue, incoming: TextFieldValue, typingMarks: Set<MarkdownStyle>?): TextFieldValue? {
        val paste = pendingPastes.firstOrNull { incoming.text.contains(it.token) }
        if (paste == null) {
            // Clipboard probes are asynchronous. Retired or foreign-session tokens must
            // never fall through to the ordinary native edit/transport path.
            return if (PRIVATE_TOKEN.containsMatchIn(incoming.text)) source else null
        }
        pendingPastes.remove(paste)
        // A stale asynchronous paste must never expose the private token or overwrite newer input.
        if (paste.source != source || paste.expected != incoming.text) return source
        val marks = MarkdownEditEngine.activeMarks(source)
        return if (MarkdownStyle.InlineCode in marks || MarkdownStyle.CodeBlock in marks) {
            MarkdownEditEngine.insertText(source, paste.plainText, typingMarks)
        } else MarkdownEditEngine.insertMarkup(source, paste.markdown, typingMarks)
    }

    private companion object {
        val PRIVATE_TOKEN = Regex("\uE000artemis:[0-9a-fA-F-]{36}\uE001")
        const val MARKDOWN = "de.tum.artemis.composer.markdown"
    }
}
