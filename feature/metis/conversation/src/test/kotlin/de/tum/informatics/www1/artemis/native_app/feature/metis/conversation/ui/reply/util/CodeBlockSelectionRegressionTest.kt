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
class CodeBlockSelectionRegressionTest {
    @Test fun `enabling code block keeps the caret so subsequent typing inserts at its original position`() {
        val before = TextFieldValue("hello world", TextRange(5))
        val code = MarkdownEditEngine.toggleCodeBlock(before)
        assertEquals("```\nhello world\n```", code.text)
        assertEquals(TextRange(5), MarkdownEditEngine.visibleValue(code).selection)
        assertTrue(MarkdownStyle.CodeBlock in MarkdownEditEngine.activeMarks(code))
        val typed = MarkdownEditEngine.insertText(code, "!")
        assertEquals("```\nhello! world\n```", typed.text)
        assertEquals(TextRange(6), MarkdownEditEngine.visibleValue(typed).selection)
    }

    @Test fun `enabling and removing code block preserves a reversed selection`() {
        val before = TextFieldValue("hello world", TextRange(8, 2))
        val code = MarkdownEditEngine.toggleCodeBlock(before)
        assertEquals(TextRange(8, 2), MarkdownEditEngine.visibleValue(code).selection)
        assertEquals("hello world", MarkdownEditEngine.visibleValue(code).text)
        val plain = MarkdownEditEngine.toggleCodeBlock(code)
        assertEquals(before, plain)
        assertEquals("heXrld", MarkdownEditEngine.insertText(plain, "X").text)
    }

    @Test fun `removing code block keeps an interior caret and leaves neighboring paragraphs intact`() {
        val before = TextFieldValue("before\n\n```kotlin\nhello world\n```\n\nafter", TextRange(23))
        val plain = MarkdownEditEngine.toggleCodeBlock(before)
        assertEquals("before\n\nhello world\n\nafter", plain.text)
        assertEquals(TextRange(13), MarkdownEditEngine.visibleValue(plain).selection)
        val typed = MarkdownEditEngine.insertText(plain, "!")
        assertEquals("before\n\nhello! world\n\nafter", MarkdownEditEngine.visibleValue(typed).text)
        assertEquals(TextRange(14), MarkdownEditEngine.visibleValue(typed).selection)
    }

    @Test fun `enabling code on a multiline selection preserves the exact selected text`() {
        val before = TextFieldValue("one\ntwo\nthree", TextRange(1, 10))
        val code = MarkdownEditEngine.toggleCodeBlock(before)
        val visible = MarkdownEditEngine.visibleValue(code)
        assertEquals("one\ntwo\nthree", visible.text)
        assertEquals(TextRange(1, 10), visible.selection)
        assertEquals("ne\ntwo\nth", visible.text.substring(visible.selection.min, visible.selection.max))
    }

    @Test fun `reversed multiline selection formats every selected paragraph and preserves direction`() {
        val before = TextFieldValue("one\ntwo\nthree", TextRange(10, 1))
        val code = MarkdownEditEngine.toggleCodeBlock(before)
        assertEquals("```\none\ntwo\nthree\n```", code.text)
        val visible = MarkdownEditEngine.visibleValue(code)
        assertEquals("one\ntwo\nthree", visible.text)
        assertEquals(TextRange(10, 1), visible.selection)
        assertEquals(before, MarkdownEditEngine.toggleCodeBlock(code))
        assertEquals("oXree", MarkdownEditEngine.visibleValue(MarkdownEditEngine.insertText(code, "X")).text)
    }

    @Test fun `code block toggles preserve the active composing word for the IME`() {
        val before = TextFieldValue("hello", TextRange(5), TextRange(0, 5))
        val code = MarkdownEditEngine.toggleCodeBlock(before)
        assertEquals("```\nhello\n```", code.text)
        assertEquals(TextRange(0, 5), MarkdownEditEngine.visibleValue(code).composition)
        assertEquals(TextRange(5), MarkdownEditEngine.visibleValue(code).selection)
        val plain = MarkdownEditEngine.toggleCodeBlock(code)
        assertEquals(before, plain)
        val committed = MarkdownEditEngine.applyVisibleEdit(code, TextFieldValue("hello!", TextRange(6)))
        assertEquals("```\nhello!\n```", committed.text)
        assertNull(committed.composition)
        assertEquals(TextRange(6), MarkdownEditEngine.visibleValue(committed).selection)
    }
}
