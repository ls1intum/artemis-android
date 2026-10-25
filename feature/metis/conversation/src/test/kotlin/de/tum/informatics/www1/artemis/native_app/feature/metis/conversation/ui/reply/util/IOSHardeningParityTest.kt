package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Wire-format cases from iOS PR 554's 78356bf correctness follow-up. */
@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class IOSHardeningParityTest {
    @Test fun `toolbar insertions before a hidden quote prefix preserve quote structure`() {
        val current = TextFieldValue("> Hello")
        assertEquals("> 😀Hello", MarkdownEditEngine.insertText(current, "😀").text)
        assertEquals("> @Hello", MarkdownEditEngine.insertText(current, "@").text)
        val channel = MarkdownEditEngine.insertText(current, "#")
        assertTrue(channel.text.startsWith("> "))
        assertEquals("#Hello", MarkdownPresentation.parse(channel.text).text)
        val linked = MarkdownEditEngine.insertLink(current, "site", "https://example.org", false)!!
        assertEquals("> [site](https://example.org)Hello", linked.text)
    }

    @Test fun `tag named file link remains distinct from its following Artemis reference`() {
        for (name in listOf("user", "channel", "faq", "attachment")) {
            val reference = "[user]Alice(alice)[/user]"
            val source = "[$name](/api/files/report.pdf) $reference"
            val p = MarkdownPresentation.parse(source)
            assertEquals("$name Alice", p.text)
            val file = MarkdownAttachments.list(source).single()
            assertEquals(name, file.name)
            assertEquals("/api/files/report.pdf", file.path)
            assertEquals(reference, p.spans.single { it.kind == MarkdownPresentation.Kind.REFERENCE }.let { source.substring(it.start, it.end) })
        }
    }

    @Test fun `combining text after atomic references retains exact payload and visible tail`() {
        for (reference in listOf("[user]Alice(alice)[/user]", "[channel]general(5)[/channel]", "[faq]Help(12)[/faq]")) {
            for (tail in listOf("\u0301", "\uFE0F", "\u200D👩")) {
                val current = TextFieldValue(reference, TextRange(reference.length))
                val edited = MarkdownEditEngine.insertText(current, tail)
                assertEquals(reference + tail, edited.text)
                val p = MarkdownPresentation.parse(edited.text)
                assertTrue(p.text, p.text.endsWith(tail))
                assertEquals(reference, p.spans.single { it.kind == MarkdownPresentation.Kind.REFERENCE }.let { edited.text.substring(it.start, it.end) })
            }
        }
    }

    @Test fun `literal private use scalars cannot collide with reference transport`() {
        val literal = String(Character.toChars(0xF0000)) + "\u0301"
        val reference = "[user]Alice(alice)[/user]"
        val source = literal + reference + "\uFE0F"
        val p = MarkdownPresentation.parse(source)
        assertEquals(literal + "Alice\uFE0F", p.text)
        val edited = MarkdownEditEngine.insertText(TextFieldValue(source, TextRange(source.length)), "!")
        assertTrue(edited.text, edited.text.startsWith(literal + reference + "\uFE0F"))
        assertEquals(literal + "Alice\uFE0F!", MarkdownPresentation.parse(edited.text).text)
    }
}
