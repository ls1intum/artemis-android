package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.MarkdownStyle
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.autocomplete.util.AutoCompleteUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.experimental.categories.Category

@Category(UnitTest::class)
class ComposerFormattingTest {
    @Test
    fun `formatting preserves selection and can be removed again`() {
        val original = TextFieldValue("hello world", TextRange(5, 0))
        val bold = MarkdownStyleUtil.toggle(MarkdownStyle.Bold, original)
        assertEquals("**hello** world", bold.text)
        assertEquals(TextRange(7, 2), bold.selection)
        assertEquals(original, MarkdownStyleUtil.toggle(MarkdownStyle.Bold, bold))
    }

    @Test
    fun `turning formatting off at the end keeps already formatted text`() {
        val result = MarkdownStyleUtil.toggle(MarkdownStyle.Bold, TextFieldValue("**hello**", TextRange(7)))
        assertEquals("**hello**", result.text)
        assertEquals(TextRange(9), result.selection)
        assertFalse(MarkdownStyleUtil.isActive(MarkdownStyle.Bold, result))
    }

    @Test
    fun `list applies to every selected line and toggles off`() {
        val source = TextFieldValue("one\ntwo\nthree", TextRange(0, 7))
        val result = MarkdownStyleUtil.toggle(MarkdownStyle.UnorderedList, source)
        assertEquals("- one\n- two\nthree", result.text)
        assertEquals("one\ntwo\nthree", MarkdownStyleUtil.toggle(MarkdownStyle.UnorderedList, result).text)
    }

    @Test
    fun `empty inline formatting does not enable send`() {
        assertFalse(MarkdownPresentation.parse("****").hasContent)
        assertFalse(MarkdownPresentation.parse("<ins></ins>").hasContent)
        assertFalse(MarkdownPresentation.parse("``").hasContent)
        assertTrue(MarkdownPresentation.parse("**hello**").hasContent)
        assertTrue(MarkdownPresentation.parse("`- `").hasContent)
    }

    @Test
    fun `presentation hides nested delimiters but preserves source offsets and emoji`() {
        val source = "Hi **bold *and italic* 👋**!"
        val result = MarkdownPresentation.parse(source, TextRange(source.length))
        assertEquals("Hi bold and italic 👋!", result.text)
        val mapping = result.offsetMapping
        val originalOffsets = (0..source.length).map(mapping::originalToTransformed)
        val visibleOffsets = (0..result.text.length).map(mapping::transformedToOriginal)
        assertEquals(originalOffsets.sorted(), originalOffsets)
        assertEquals(visibleOffsets.sorted(), visibleOffsets)
        assertTrue(originalOffsets.all { it in 0..result.text.length })
        assertTrue(visibleOffsets.all { it in 0..source.length })
        assertTrue(result.spans.any { it.style == MarkdownStyle.Bold })
        assertTrue(result.spans.any { it.style == MarkdownStyle.Italic })
    }

    @Test
    fun `code is literal and malformed markup stays editable`() {
        assertEquals("**literal**", MarkdownPresentation.parse("`**literal**`").text)
        assertEquals("**unfinished", MarkdownPresentation.parse("**unfinished").text)
        assertEquals("Ada", MarkdownPresentation.parse("[user]Ada(ada)[/user]").text)
        assertEquals("hello", MarkdownPresentation.parse("\\*hello\\*").text.trim('*'))
    }

    @Test
    fun `links validate destinations and preserve selection text`() {
        val value = TextFieldValue("Read this", TextRange(5, 9))
        val result = MarkdownStyleUtil.insertLink(value, "this", "https://example.com/a(b)")!!
        assertEquals("Read [this](https://example.com/a\\(b\\))", result.text)
        assertEquals("Read this", MarkdownPresentation.parse(result.text).text)
        assertEquals(null, MarkdownStyleUtil.insertLink(value, "this", "javascript:alert(1)"))
    }

    @Test
    fun `backspace after formatted text removes a visible character rather than a hidden delimiter`() {
        val previous = TextFieldValue("**hello**", TextRange(9))
        val result = MarkdownStyleUtil.normalizeEdit(previous, TextFieldValue("**hello*", TextRange(8)))
        assertEquals("**hell**", result.text)
        assertEquals(TextRange(6), result.selection)
    }

    @Test
    fun `turning off part of a selection preserves formatting on both sides`() {
        val result = MarkdownStyleUtil.toggle(MarkdownStyle.Bold, TextFieldValue("**hello**", TextRange(3, 6)))
        val presentation = MarkdownPresentation.parse(result.text)
        assertEquals("hello", presentation.text)
        assertEquals(2, presentation.spans.count { it.style == MarkdownStyle.Bold })
        assertFalse(MarkdownStyleUtil.isActive(MarkdownStyle.Bold, result))
    }

    @Test
    fun `removing one style from a nested selection preserves the other style`() {
        val result = MarkdownStyleUtil.toggle(MarkdownStyle.Bold, TextFieldValue("***hello***", TextRange(4, 7)))
        assertEquals("hello", MarkdownPresentation.parse(result.text).text)
        assertTrue(MarkdownStyleUtil.isActive(MarkdownStyle.Italic, result))
        assertFalse(MarkdownStyleUtil.isActive(MarkdownStyle.Bold, result))
    }

    @Test
    fun `Artemis references display their label and keep their source payload intact`() {
        val source = "[user]Ada Lovelace(ada)[/user] [channel]general(42)[/channel]"
        val result = MarkdownPresentation.parse(source, TextRange(source.length))
        assertEquals("Ada Lovelace general", result.text)
        assertEquals(source.length, result.offsetMapping.transformedToOriginal(result.text.length))
    }

    @Test
    fun `code fences preserve literal list markers and separate following paragraphs`() {
        assertEquals("- literal\nnext", MarkdownPresentation.parse("```\n- literal\n```\nnext").text)
    }

    @Test
    fun `combined formatting is reversible without losing the other style`() {
        val bold = MarkdownStyleUtil.toggle(MarkdownStyle.Bold, TextFieldValue("hello", TextRange(0, 5)))
        val both = MarkdownStyleUtil.toggle(MarkdownStyle.Italic, bold)
        val italic = MarkdownStyleUtil.toggle(MarkdownStyle.Bold, both)
        assertEquals("*hello*", italic.text)
        assertTrue(MarkdownStyleUtil.isActive(MarkdownStyle.Italic, italic))
        assertFalse(MarkdownStyleUtil.isActive(MarkdownStyle.Bold, italic))
    }

    @Test
    fun `empty combined styles are not message content`() {
        val bold = MarkdownStyleUtil.toggle(MarkdownStyle.Bold, TextFieldValue())
        val both = MarkdownStyleUtil.toggle(MarkdownStyle.Italic, bold)
        assertFalse(MarkdownPresentation.parse(both.text).hasContent)
        val italic = MarkdownStyleUtil.toggle(MarkdownStyle.Bold, both)
        assertTrue(MarkdownStyleUtil.isActive(MarkdownStyle.Italic, italic))
        assertFalse(MarkdownStyleUtil.isActive(MarkdownStyle.Bold, italic))
    }

    @Test
    fun `typing whitespace does not reveal formatting syntax`() {
        val source = TextFieldValue("**hello**", TextRange(7))
        val result = MarkdownEditEngine.applyVisibleEdit(source, TextFieldValue("hello ", TextRange(6)))
        assertEquals("hello ", MarkdownPresentation.parse(result.text).text)
        assertTrue(MarkdownStyleUtil.isActive(MarkdownStyle.Bold, result))
    }

    @Test
    fun `forward delete at the start of formatted text keeps the deletion`() {
        val next = TextFieldValue("**ello**", TextRange(2))
        assertEquals(next, MarkdownStyleUtil.normalizeEdit(TextFieldValue("**hello**", TextRange(2)), next))
    }

    @Test
    fun `native formatting keeps whitespace outside valid delimiters before sending`() {
        val result = MarkdownStyleUtil.toggle(MarkdownStyle.Bold, TextFieldValue(" hello ", TextRange(0, 7)))
        assertEquals(" **hello** ", MarkdownStyleUtil.prepareForSend(result.text))
        // Existing source is preserved, including deliberately literal invalid Markdown.
        assertEquals("** hello **", MarkdownStyleUtil.prepareForSend("** hello **"))
    }

    @Test
    fun `mention query excludes closing styles and text after the caret`() {
        val value = TextFieldValue("**@ada** later", TextRange(6))
        assertEquals("ada", AutoCompleteUtil.query(value, listOf('@'))?.text)
        val replaced = AutoCompleteUtil.perform(value, listOf('@'), "[user]Ada(ada)[/user]")
        assertEquals("**[user]Ada(ada)[/user]** later", replaced.text)
        val afterClosingStyle = value.copy(selection = TextRange(8))
        assertEquals("**[user]Ada(ada)[/user]** later",
            AutoCompleteUtil.perform(afterClosingStyle, listOf('@'), "[user]Ada(ada)[/user]").text)
    }

    @Test
    fun `a link edit preserves an upload that completed while the dialog was open`() {
        val original = TextFieldValue("Read this", TextRange(5, 9))
        val current = TextFieldValue("Read this\n[file](/api/core/files/file.pdf)")
        val edited = MarkdownStyleUtil.insertLink(original, "this", "https://example.com")!!
        assertEquals("Read [this](https://example.com)\n[file](/api/core/files/file.pdf)",
            MarkdownStyleUtil.rebaseEdit(original, current, edited)?.text)
    }

    @Test
    fun `an unchanged link label does not accumulate escapes`() {
        val original = MarkdownStyleUtil.insertLink(TextFieldValue(), "A [B]", "https://example.com")!!
        val span = MarkdownPresentation.parse(original.text).spans.single()
        val label = MarkdownPresentation.parse(original.text.substring(span.contentStart, span.contentEnd)).text
        val reapplied = MarkdownStyleUtil.insertLink(original.copy(selection = TextRange(0, original.text.length)), label, "https://example.com")!!
        assertEquals(original.text, reapplied.text)
        assertEquals("A [B]", MarkdownPresentation.parse(reapplied.text).text)
    }
}
