package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.assertCursorAfterSubstring
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.MarkdownStyle
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class MarkdownStyleUtilTest {

    @Test
        fun `test GIVEN an empty selection WHEN calling apply THEN tags should be inserted at cursor position`() {
            val text = "hello"
            val textFieldValue = TextFieldValue(
                text = text,
                selection = TextRange(text.length, text.length)
            )

            val result = MarkdownStyleUtil.apply(
                style = MarkdownStyle.Italic,
                currentTextFieldValue = textFieldValue
            )

            assertEquals("hello**", result.text)
            result.assertCursorAfterSubstring("hello*")
        }

        @Test
        fun `test GIVEN a selection with start less than end WHEN calling apply THEN tags should be wrapped around selection`() {
            val text = "hello"
            val textFieldValue = TextFieldValue(
                text = text,
                selection = TextRange(0, text.length)
            )

            val result = MarkdownStyleUtil.apply(
                style = MarkdownStyle.Italic,
                currentTextFieldValue = textFieldValue
            )

            assertEquals("*hello*", result.text)
            result.assertCursorAfterSubstring("*hello*")
        }

    @Test
    fun `test GIVEN a selection with start after end WHEN calling apply THEN no error should be thrown`() {
        val text = "hello"
        val textFieldValue = TextFieldValue(
            text = text,
            selection = TextRange(text.length, 0)
        )

        val result = MarkdownStyleUtil.apply(
            style = MarkdownStyle.Italic,
            currentTextFieldValue = textFieldValue
        )

        assertEquals("*hello*", result.text)
        result.assertCursorAfterSubstring("*hello*")
    }
}