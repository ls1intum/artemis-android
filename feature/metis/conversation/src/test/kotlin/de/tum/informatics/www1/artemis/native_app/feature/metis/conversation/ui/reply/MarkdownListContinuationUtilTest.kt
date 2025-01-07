package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith

@Category(UnitTest::class)
@RunWith(AndroidJUnit4::class)
class MarkdownListContinuationUtilTest {

    @Test
    fun `test WHEN typing newline after an unordered list line THEN next line continues with dash prefix`() {
        val oldText = "- Hello"

        val newText = "- Hello\n"
        val cursor = newText.length

        val newValue = TextFieldValue(
            text = newText,
            selection = TextRange(cursor, cursor)
        )

        val result = continueListIfApplicable(oldText, newValue)

        val expectedText = "- Hello\n- "
        assertEquals(expectedText, result.text)
        assertEquals(expectedText.length, result.selection.start)
        assertEquals(expectedText.length, result.selection.end)
    }

    @Test
    fun `test WHEN typing newline after an ordered list line THEN next line increments the list number`() {
        val oldText = "1. Hello"

        val newText = "1. Hello\n"
        val cursor = newText.length

        val newValue = TextFieldValue(
            text = newText,
            selection = TextRange(cursor, cursor)
        )

        val result = continueListIfApplicable(oldText, newValue)

        val expectedText = "1. Hello\n2. "
        assertEquals(expectedText, result.text)
        assertEquals(expectedText.length, result.selection.start)
        assertEquals(expectedText.length, result.selection.end)
    }

    @Test
    fun `test WHEN newline after non-list line THEN no prefix is inserted`() {
        val oldText = "Hello"

        val newText = "Hello\n"
        val cursor = newText.length

        val newValue = TextFieldValue(
            text = newText,
            selection = TextRange(cursor, cursor)
        )

        val result = continueListIfApplicable(oldText, newValue)

        val expectedText = "Hello\n"
        assertEquals(expectedText, result.text)
        assertEquals(expectedText.length, result.selection.start)
        assertEquals(expectedText.length, result.selection.end)
    }
}


