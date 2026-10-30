package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import org.junit.Test
import org.junit.experimental.categories.Category
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Category(UnitTest::class)
class MarkdownAttachmentsTest {
    @Test
    fun `lists uploaded files and images in source order without ordinary links or code`() {
        val source = "[website](https://example.org) [course](/courses/1) " +
            "`[literal](/api/files/code.pdf)` [file](/api/core/files/report.pdf) " +
            "![image](https://example.org/picture.png) [legacy](/files/old.pdf) " +
            "[query](https://example.org/?next=/api/files/not-an-upload)"

        val attachments = MarkdownAttachments.list(source)

        assertEquals(listOf("file", "image", "legacy"), attachments.map { it.name })
        assertEquals(listOf(false, true, false), attachments.map { it.isImage })
        assertEquals(listOf("/api/core/files/report.pdf", "https://example.org/picture.png", "/files/old.pdf"), attachments.map { it.path })
        attachments.forEach { assertEquals(it.originalMarkup, source.substring(it.start, it.end)) }
    }

    @Test
    fun `adjacent copies of one file can be renamed and removed independently`() {
        val original = TextFieldValue("[one](/api/files/file.pdf)[two](/api/files/file.pdf)")
        val second = MarkdownAttachments.list(original.text)[1]

        val renamed = assertNotNull(MarkdownAttachments.rename(original, second, "Renamed"))
        assertEquals("[one](/api/files/file.pdf)[Renamed](/api/files/file.pdf)", renamed.text)
        val removed = assertNotNull(MarkdownAttachments.remove(renamed, MarkdownAttachments.list(renamed.text)[1]))
        assertEquals("[one](/api/files/file.pdf)", removed.text)
    }

    @Test
    fun `rename preserves exact escaped relative destination and enclosing marks`() {
        val original = TextFieldValue("**Before [report](</api/core/files/a\\(final\\).pdf?download=1&x=2>) after**")
        val attachment = MarkdownAttachments.list(original.text).single()

        val result = assertNotNull(MarkdownAttachments.rename(original, attachment, "  Report [final] 😀  "))

        assertEquals("**Before [Report \\[final\\] 😀](</api/core/files/a\\(final\\).pdf?download=1&x=2>) after**", result.text)
        assertEquals("/api/core/files/a(final).pdf?download=1&x=2", MarkdownAttachments.list(result.text).single().path)
        assertEquals("Report [final] 😀", MarkdownAttachments.list(result.text).single().name)
    }

    @Test
    fun `image rename escapes markdown names and survives restored source`() {
        val original = TextFieldValue("Hi 😀 ![same](/api/files/one.png) ![same](/api/files/two.png)")
        val name = "Diagram [2] *bold* _under_ `code` <ins> & \\ 😀"
        val result = assertNotNull(MarkdownAttachments.rename(original, MarkdownAttachments.list(original.text).last(), name))

        assertTrue(result.text.startsWith("Hi 😀 ![same](/api/files/one.png) !["))
        val restored = TextFieldValue(result.text)
        assertEquals(listOf("same", name), MarkdownAttachments.list(restored.text).map { it.name })
        assertEquals(listOf("/api/files/one.png", "/api/files/two.png"), MarkdownAttachments.list(restored.text).map { it.path })
        assertTrue(MarkdownAttachments.list(restored.text).all { it.isImage })
    }

    @Test
    fun `reference tag names remain attachments after rename and restore`() {
        for (name in listOf("text", "user", "lecture", "message", "file-upload")) {
            val original = TextFieldValue("[original](/api/files/a)")
            val result = assertNotNull(MarkdownAttachments.rename(original, MarkdownAttachments.list(original.text).single(), name))
            val restored = MarkdownAttachments.list(result.text).single()
            assertEquals(name, restored.name)
            assertEquals("/api/files/a", restored.path)
        }
    }

    @Test
    fun `blank multiline and control names are rejected`() {
        val original = TextFieldValue("[file](/api/files/file.pdf)")
        val attachment = MarkdownAttachments.list(original.text).single()

        for (invalid in listOf("", "   ", "two\nlines", "two\rlines", "two\tlines", "two\u0000lines", "two\u2028lines")) {
            assertNull(MarkdownAttachments.rename(original, attachment, invalid), invalid)
        }
        assertEquals("[file](/api/files/file.pdf)", original.text)
    }

    @Test
    fun `stale actions cannot alter replaced upload targets or another draft`() {
        val original = "[file](/api/files/file.pdf)"
        val attachment = MarkdownAttachments.list(original).single()
        for (changed in listOf("Different text", "[file](/api/files/new.pdf)", "Prefix $original")) {
            val current = TextFieldValue(changed)
            assertNull(MarkdownAttachments.rename(current, attachment, "renamed"))
            assertNull(MarkdownAttachments.remove(current, attachment))
            assertEquals(changed, current.text)
        }
    }

    @Test
    fun `rename after an upload completes retains the new upload and current cursor`() {
        val original = "[file](/api/files/file.pdf)"
        val attachment = MarkdownAttachments.list(original).single()
        val currentSource = "$original\n![new](/api/files/new.png)"
        val current = TextFieldValue(currentSource, TextRange(currentSource.length), TextRange(currentSource.length - 1, currentSource.length))

        val result = assertNotNull(MarkdownAttachments.rename(current, attachment, "report"))

        assertEquals("[report](/api/files/file.pdf)\n![new](/api/files/new.png)", result.text)
        assertEquals(TextRange(result.text.length), result.selection)
        assertNull(result.composition)
    }

    @Test
    fun `removal preserves surrounding source and maps a reversed selection`() {
        val source = "**Hi [file](/api/files/file.pdf) there**"
        val attachment = MarkdownAttachments.list(source).single()
        val current = TextFieldValue(source, TextRange(source.length, 0))

        val removed = assertNotNull(MarkdownAttachments.remove(current, attachment))

        assertEquals("**Hi  there**", removed.text)
        assertEquals(TextRange(removed.text.length, 0), removed.selection)
    }

    @Test
    fun `rename and remove undo redo the entire payload and restore drafts`() {
        val initial = TextFieldValue("*![diagram](/api/files/a\\(1\\).png)* [report](/api/files/report.pdf)")
        val history = ComposerHistory()
        val renamed = assertNotNull(MarkdownAttachments.rename(initial, MarkdownAttachments.list(initial.text).first(), "Diagram [1] 😀"))
        history.commit(initial, renamed)
        val removed = assertNotNull(MarkdownAttachments.remove(renamed, MarkdownAttachments.list(renamed.text).last()))
        history.commit(renamed, removed)

        assertEquals(renamed, history.undo())
        assertEquals(initial, history.undo())
        assertFalse(history.canUndo)
        assertEquals(renamed, history.redo())
        assertEquals(removed, history.redo())
        val restored = MarkdownAttachments.list(TextFieldValue(removed.text).text).single()
        assertEquals("Diagram [1] 😀", restored.name)
        assertEquals("/api/files/a(1).png", restored.path)
        assertEquals("*![Diagram \\[1\\] 😀](/api/files/a\\(1\\).png)* ", removed.text)
    }
}
