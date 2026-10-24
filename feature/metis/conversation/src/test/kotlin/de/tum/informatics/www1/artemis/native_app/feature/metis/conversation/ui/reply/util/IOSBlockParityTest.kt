package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.MarkdownStyle
import org.junit.Assert.*
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class IOSBlockParityTest {
    @Test fun `quotes expose only body text with source mapped selections`() {
        val source = "> **hello** 👋\n> [user]Ada(ada)[/user]"
        val presentation = MarkdownPresentation.parse(source)

        assertEquals("hello 👋\nAda", presentation.text)
        assertEquals(4, presentation.offsetMapping.transformedToOriginal(0))
        assertEquals(TextRange(0, 8), presentation.visibleRange(presentation.spans.first { it.style == MarkdownStyle.Blockquote }))
        val quote = caret(source, 6)
        val edited = MarkdownEditEngine.insertText(quote, "🙂")
        assertEquals("> **hello** 🙂👋\n> [user]Ada(ada)[/user]", edited.text)
        assertEquals(TextRange(8), MarkdownEditEngine.visibleValue(edited).selection)
    }

    @Test fun `quote Enter continues a quote and exits its empty final paragraph`() {
        val continued = MarkdownEditEngine.insertText(caret("> hello", 5), "\n")
        assertEquals("> hello\n> ", continued.text)
        assertEquals("hello\n", MarkdownEditEngine.visibleValue(continued).text)
        assertTrue(MarkdownStyleUtil.isActive(MarkdownStyle.Blockquote, continued))

        val exited = MarkdownEditEngine.insertText(continued, "\n")
        assertEquals("> hello\n\n", exited.text)
        assertFalse(MarkdownStyleUtil.isActive(MarkdownStyle.Blockquote, exited))
        assertEquals("> hello\n\nAfter", MarkdownEditEngine.insertText(exited, "After").text)
    }

    @Test fun `multiline native paste continues every list and quote paragraph`() {
        for ((source, position, expected) in listOf(
            Triple("- Hello", 4, "- Heone\n- twollo"),
            Triple("7. Hello", 5, "7. Heone\n8. twollo"),
            Triple("> Hello", 2, "> Heone\n> twollo"),
        )) {
            val result = MarkdownEditEngine.insertText(caret(source, position), "one\ntwo")
            assertEquals(source, expected, result.text)
        }
    }

    @Test fun `ordered Enter renumbers following items while preserving references`() {
        val source = "9. first\n10. [user]Ada(ada)[/user]"
        val result = MarkdownEditEngine.insertText(caret(source, 8), "\n")
        assertEquals("9. first\n10. \n11. [user]Ada(ada)[/user]", result.text)
        assertEquals(TextRange(13), MarkdownEditEngine.visibleValue(result).selection)
        assertEquals("9. first\n10. inserted\n11. [user]Ada(ada)[/user]", MarkdownEditEngine.insertText(result, "inserted").text)
    }

    @Test fun `final empty code paragraph Enter exits and keeps language and literal payloads`() {
        val source = "```kotlin\n[user]Ada(ada)[/user]\n\n```"
        val result = MarkdownEditEngine.insertText(caret(source, 22), "\n")
        assertEquals("```kotlin\n[user]Ada(ada)[/user]\n```\n\n", result.text)
        assertFalse(MarkdownStyleUtil.isActive(MarkdownStyle.CodeBlock, result))
        assertEquals("```kotlin\n[user]Ada(ada)[/user]\n```\n\nAfter", MarkdownEditEngine.insertText(result, "After").text)
    }

    @Test fun `empty code Enter exits without removing its fence`() {
        val result = MarkdownEditEngine.insertText(caret("```swift\n```", 0), "\n")
        assertEquals("```swift\n```\n\n", result.text)
        assertFalse(MarkdownStyleUtil.isActive(MarkdownStyle.CodeBlock, result))
    }

    @Test fun `ordinary code Enter remains literal until its final paragraph is empty`() {
        val source = "```kotlin\none\ntwo\n```"
        val result = MarkdownEditEngine.insertText(caret(source, 3), "\n")
        assertEquals("```kotlin\none\n\ntwo\n```", result.text)
        assertTrue(MarkdownStyleUtil.isActive(MarkdownStyle.CodeBlock, result))
    }

    @Test fun `software backspace at the second quote body start removes quote structure`() {
        val current = caret("> first\n> second", 6)
        val result = MarkdownEditEngine.applyVisibleEdit(current, TextFieldValue("firstsecond", TextRange(5)))
        assertEquals("> first\n\nsecond", result.text)
        assertEquals(TextRange(7), MarkdownEditEngine.visibleValue(result).selection)
    }

    @Test fun `composition keeps its native replacement without block continuation`() {
        val current = caret("> hello", 5)
        val result = MarkdownEditEngine.applyVisibleEdit(current, TextFieldValue("hello\nx", TextRange(7), TextRange(6, 7)))
        assertEquals("hello\nx", MarkdownEditEngine.visibleValue(result).text)
        assertEquals(TextRange(6, 7), MarkdownEditEngine.visibleValue(result).composition)
    }

    @Test fun `rich multiline paste keeps paragraph structure and exact reference payloads`() {
        val markup = "**one**\n[user]Ada(ada)[/user]"
        for ((source, position, expected) in listOf(
            Triple("- Hello", 4, "- He**one**\n- [user]Ada(ada)[/user]llo"),
            Triple("7. Hello", 5, "7. He**one**\n8. [user]Ada(ada)[/user]llo"),
            Triple("> Hello", 2, "> He**one**\n> [user]Ada(ada)[/user]llo"),
        )) {
            val result = MarkdownEditEngine.insertMarkup(caret(source, position), markup)
            assertEquals(source, expected, result.text)
        }
    }

    @Test fun `rich paste combines pending inline marks with pasted marks`() {
        val result = MarkdownEditEngine.insertMarkup(TextFieldValue(), "*pasted*", setOf(MarkdownStyle.Bold))
        val presentation = MarkdownPresentation.parse(result.text)
        assertEquals("pasted", presentation.text)
        assertTrue(MarkdownDocument.glyphs(presentation).all { MarkdownStyle.Bold in it.marks && MarkdownStyle.Italic in it.marks })
    }

    @Test fun `hardware backspace can remove the first quote prefix without exposing markers`() {
        val source = "> **hello**"
        val result = MarkdownEditEngine.removeBlockAtStart(caret(source, 0))!!
        assertEquals("**hello**", result.text)
        assertEquals(TextRange(0), MarkdownEditEngine.visibleValue(result).selection)
        assertNull(MarkdownEditEngine.removeBlockAtStart(caret(source, 2)))
        assertNull(MarkdownEditEngine.removeBlockAtStart(caret(source, 0).copy(composition = TextRange(4, 5))))
    }

    @Test fun `replacing the whole quote body with a link keeps the quote`() {
        val result = MarkdownStyleUtil.insertLink(TextFieldValue("> hello", TextRange(2, 7)), "hello", "https://example.com")!!
        assertEquals("> [hello](https://example.com)", result.text)
        assertEquals("hello", MarkdownEditEngine.visibleValue(result).text)
    }

    @Test fun `restored quote caret before its hidden prefix still edits the body`() {
        val current = TextFieldValue("> hello")
        assertEquals("> Xhello", MarkdownEditEngine.insertText(current, "X").text)
        assertEquals("hello", MarkdownEditEngine.removeBlockAtStart(current)?.text)
        assertEquals("", MarkdownEditEngine.insertText(TextFieldValue("> "), "\n").text)
    }

    private fun caret(source: String, visible: Int): TextFieldValue {
        val presentation = MarkdownPresentation.parse(source)
        return TextFieldValue(source, TextRange(presentation.offsetMapping.transformedToOriginal(visible)))
    }
}
