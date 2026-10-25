package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.test.core.app.ApplicationProvider
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.MarkdownRenderFactory
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.MarkdownStyle
import org.junit.Assert.*
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class ComposerReliabilityTest {
    private fun rendered(source: String) = MarkdownRenderFactory.create(ApplicationProvider.getApplicationContext(), null, null).toMarkdown(source).toString().trim('\u00a0')

    @Test fun `code is literal inside outer emphasis`() {
        assertEquals("bold literal ** here tail", MarkdownPresentation.parse("**bold `literal ** here` tail**").text)
        assertEquals("foo``bar", MarkdownPresentation.parse("`foo``bar`").text)
    }
    @Test fun `list bullets cannot become italic delimiters`() {
        assertEquals("• one\n• two", MarkdownPresentation.parse("* one\n* two").text)
    }
    @Test fun `formatting can cross existing span boundaries`() {
        val result = MarkdownStyleUtil.toggle(MarkdownStyle.Italic, TextFieldValue("**hello** world", TextRange(3, 12)))
        assertEquals("hello world", MarkdownPresentation.parse(result.text).text)
        assertEquals("hello world", rendered(result.text))
    }
    @Test fun `toggle removes a style across multiple spans`() {
        val result = MarkdownStyleUtil.toggle(MarkdownStyle.Bold, TextFieldValue("**hello** **world**", TextRange(2, 16)))
        assertEquals("hello world", MarkdownPresentation.parse(result.text).text)
        assertFalse(MarkdownStyleUtil.isActive(MarkdownStyle.Bold, result))
    }
    @Test fun `inline and fenced code choose safe delimiters`() {
        val body = "use `x`"
        val literal = MarkdownEditEngine.insertText(TextFieldValue(), body)
        val inline = MarkdownStyleUtil.toggle(MarkdownStyle.InlineCode, literal.copy(selection = TextRange(0, literal.text.length)))
        assertEquals(body, rendered(inline.text))
        val blockBody = "line\n```\nnext"
        val block = MarkdownStyleUtil.toggle(MarkdownStyle.CodeBlock, TextFieldValue(blockBody, TextRange(0, blockBody.length)))
        assertEquals(blockBody, MarkdownPresentation.parse(block.text).text)
        assertEquals(rendered("````\n$blockBody\n````"), rendered(block.text))
    }
    @Test fun `emphasis respects paragraph boundaries`() {
        val value = "hello\n\nworld"
        val bold = MarkdownStyleUtil.toggle(MarkdownStyle.Bold, TextFieldValue(value, TextRange(0, value.length)))
        assertEquals(value, rendered(bold.text))
    }
    @Test fun `all escaped punctuation decodes before rendering`() {
        val link = MarkdownStyleUtil.insertLink(TextFieldValue(), "R&D [notes]", "https://example.com")!!
        assertEquals("R&D [notes]", MarkdownPresentation.parse(link.text).text)
        assertEquals("R&D [notes]", rendered(link.text))
    }
    @Test fun `empty alt image is valid message content`() {
        assertTrue(MarkdownPresentation.parse("![](https://example.com/image.png)").hasContent)
    }
    @Test fun `visible replacements crossing marks preserve surrounding characters and styles`() {
        val source = TextFieldValue("**hello** world", TextRange(3, 12))
        val result = MarkdownEditEngine.applyVisibleEdit(source, TextFieldValue("hXrld", TextRange(2)))
        assertEquals("hXrld", MarkdownEditEngine.visibleValue(result).text)
        assertEquals("hXrld", rendered(result.text))
    }
    @Test fun `native cut and escaped punctuation deletion cannot expose source markers`() {
        val source = TextFieldValue("**hello** world", TextRange(3, 12))
        assertEquals("hrld", MarkdownEditEngine.visibleValue(MarkdownEditEngine.applyVisibleEdit(source, TextFieldValue("hrld", TextRange(1)))).text)
        val escaped = TextFieldValue("A \\[B\\]", TextRange(2))
        val result = MarkdownEditEngine.applyVisibleEdit(escaped, TextFieldValue("A B]", TextRange(2)))
        assertEquals("A B]", rendered(result.text))
    }
    @Test fun `native insertion preserves active marks after whitespace and composition`() {
        val pending = MarkdownStyleUtil.toggle(MarkdownStyle.Bold, TextFieldValue())
        val first = MarkdownEditEngine.applyVisibleEdit(pending, TextFieldValue("hello ", TextRange(6), TextRange(0, 5)))
        assertTrue(MarkdownStyleUtil.isActive(MarkdownStyle.Bold, first))
        val second = MarkdownEditEngine.applyVisibleEdit(first, TextFieldValue("hello world", TextRange(11)))
        assertEquals("hello world", rendered(second.text))
        assertTrue(MarkdownStyleUtil.isActive(MarkdownStyle.Bold, second))
    }
    @Test fun `partial hyperlink label styling retains destination and unselected marks`() {
        val original = TextFieldValue("**[hello](https://example.com)**", TextRange(4, 7))
        val result = MarkdownStyleUtil.toggle(MarkdownStyle.Bold, original)
        val p = MarkdownPresentation.parse(result.text)
        assertEquals("hello", p.text)
        assertTrue(p.spans.any { it.destination == "https://example.com" })
        assertFalse(MarkdownStyleUtil.isActive(MarkdownStyle.Bold, result))
    }
    @Test fun `native atom replacement cannot leave transport fragments`() {
        val source = TextFieldValue("Hi [user]Ada Lovelace(ada)[/user]!", TextRange(10, 13))
        val visible = MarkdownEditEngine.visibleValue(source)
        val next = TextFieldValue(visible.text.replaceRange(visible.selection.min, visible.selection.max, "X"), TextRange(visible.selection.min + 1))
        val result = MarkdownEditEngine.applyVisibleEdit(source, next)
        assertEquals("Hi X!", MarkdownEditEngine.visibleValue(result).text)
        assertFalse(result.text.contains("[/user]"))
    }
    @Test fun `undo restores marks and exact reference payloads`() {
        val source = TextFieldValue("[user]Ada(ada)[/user]", TextRange(0, 20))
        val history = ComposerHistory()
        val changed = MarkdownEditEngine.insertText(source, "replacement")
        history.commit(source, changed)
        assertEquals(source.text, history.undo()?.text)
        assertEquals(changed.text, history.redo()?.text)
    }
    @Test fun `literal references stay literal inside code and when escaped`() {
        val body = "[user]Ada(ada)[/user]"
        for (source in listOf("`$body`", "```\n$body\n```", "\\$body")) {
            assertFalse(source, MarkdownPresentation.parse(source).spans.any { it.kind == MarkdownPresentation.Kind.REFERENCE })
            assertEquals(source, MarkdownStyleUtil.prepareForSend(source))
        }
    }
    @Test fun `HTML code retains literal references and decodes HTML character entities`() {
        for (tag in listOf("code", "CODE")) {
            val source = "<$tag>[user]Ada(ada)[/user] &lt;T&gt; &amp; x</$tag>"
            val p = MarkdownPresentation.parse(source)
            assertEquals("[user]Ada(ada)[/user] <T> & x", p.text)
            assertFalse(p.spans.any { it.isAtomic })
            val updated = MarkdownEditEngine.insertText(TextFieldValue(source, TextRange(source.indexOf("</"))), "!")
            assertEquals(p.text + "!", MarkdownPresentation.parse(updated.text).text)
            assertEquals(rendered("`" + p.text + "!`"), rendered(updated.text))
        }
    }
    @Test fun `HTML code tags inside Markdown code stay independent literals`() {
        val source = "`<code>` *outside* `</code>`"
        val p = MarkdownPresentation.parse(source)
        assertEquals("<code> outside </code>", p.text)
        assertEquals(2, p.spans.count { it.style == MarkdownStyle.InlineCode })
        assertEquals(1, p.spans.count { it.style == MarkdownStyle.Italic })
    }
    @Test fun `HTML code cannot hide punctuation through crossing emphasis`() {
        for ((source, visible) in listOf("<code>*a</code>*" to "*a*", "*<code>a*</code>" to "*a*")) {
            val p = MarkdownPresentation.parse(source)
            assertEquals(visible, p.text)
            assertFalse(p.spans.any { it.style == MarkdownStyle.Italic })
            val changed = MarkdownEditEngine.insertText(TextFieldValue(source, TextRange(source.length)), "X", emptySet())
            assertEquals(visible + "X", MarkdownPresentation.parse(changed.text).text)
        }
    }
    @Test fun `unsupported source metadata survives an untouched send`() {
        for (source in listOf("<https://example.com>", "---", "text <span>red</span>", "[x](https://example.com \"Title\")", "- a\n  - b", "> > nested", "See \$x^2\$ here")) {
            assertEquals(source, source, MarkdownStyleUtil.prepareForSend(source))
        }
    }
    @Test fun `default code caret keeps fenced block valid and retains language`() {
        val source = "```kotlin\nhello\n```"
        for (caret in listOf(0, source.length)) {
            val current = TextFieldValue(source, TextRange(caret))
            val old = MarkdownEditEngine.visibleValue(current)
            val result = MarkdownEditEngine.applyVisibleEdit(current, TextFieldValue(old.text.replaceRange(old.selection.start, old.selection.start, "X"), TextRange(old.selection.start + 1)))
            val expected = if (caret == 0) "Xhello" else "helloX"
            assertEquals(expected, MarkdownPresentation.parse(result.text).text)
            assertEquals(rendered("```kotlin\n$expected\n```"), rendered(result.text))
            assertTrue(result.text.contains("```kotlin"))
        }
    }
    @Test fun `all small visible selections preserve characters across marks and repeated text`() {
        for (source in listOf("aaaa", "a  b", "**aa**aa", "***ab*** c", "[xy](https://example.com)")) {
            val p = MarkdownPresentation.parse(source)
            for (from in 0..p.text.length) for (to in from..p.text.length) for (replacement in listOf("", "a", "X", "👋")) {
                val old = TextFieldValue(source, TextRange(p.offsetMapping.transformedToOriginal(from), p.offsetMapping.transformedToOriginal(to)))
                val expected = p.text.replaceRange(from, to, replacement)
                val result = MarkdownEditEngine.applyVisibleEdit(old, TextFieldValue(expected, TextRange(from + replacement.length)))
                assertEquals("$source $from..$to '$replacement'", expected, MarkdownEditEngine.visibleValue(result).text)
                assertEquals(TextRange(from + replacement.length), MarkdownEditEngine.visibleValue(result).selection)
            }
        }
    }
    @Test fun `plain typing next to underscore emphasis retains existing marks`() {
        for (source in listOf("_hello_", "__hello__")) for (caret in listOf(0, source.length)) {
            val result = MarkdownEditEngine.insertText(TextFieldValue(source, TextRange(caret)), "x", emptySet())
            val p = MarkdownPresentation.parse(result.text)
            assertEquals(if (caret == 0) "xhello" else "hellox", p.text)
            val mark = if (source.startsWith("__")) MarkdownStyle.Bold else MarkdownStyle.Italic
            assertEquals(5, MarkdownDocument.glyphs(p).count { mark in it.marks })
            assertEquals(p.text, rendered(result.text))
        }
    }
    @Test fun `native Enter persists valid list and quote prefixes and exits empty items`() {
        for ((source, expected) in listOf("- one" to "- one\n- ", "> quote" to "> quote\n> ", "1. one" to "1. one\n2. ")) {
            val first = MarkdownEditEngine.insertText(TextFieldValue(source, TextRange(source.length)), "\n")
            assertEquals(expected, first.text)
            val blockStyle = when (source.first()) { '-' -> MarkdownStyle.UnorderedList; '>' -> MarkdownStyle.Blockquote; else -> MarkdownStyle.OrderedList }
            assertTrue(source, MarkdownStyleUtil.isActive(blockStyle, first))
            val typed = MarkdownEditEngine.insertText(first, "next")
            assertEquals(expected + "next", typed.text)
            val second = MarkdownEditEngine.insertText(first, "\n")
            assertFalse(second.text.contains('•'))
            assertFalse(second.text.contains('│'))
            assertEquals(2, MarkdownEditEngine.visibleValue(second).text.count { it == '\n' })
        }
    }
    @Test fun `editing a long marked draft preserves unrelated source fragments`() {
        val source = "**a**b".repeat(300)
        val visible = "ab".repeat(300)
        val result = MarkdownEditEngine.applyVisibleEdit(TextFieldValue(source, TextRange(source.length)), TextFieldValue(visible + "x", TextRange(visible.length + 1)))
        assertEquals(source + "x", result.text)
    }
}
