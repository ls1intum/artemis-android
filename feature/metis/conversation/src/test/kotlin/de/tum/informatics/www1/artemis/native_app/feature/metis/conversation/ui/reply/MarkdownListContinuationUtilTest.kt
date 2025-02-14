package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.MarkdownListContinuationUtil.continueListIfApplicable
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
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

    @Test
    fun `test WHEN typing newline in the middle of multi-line text after ordered list THEN next line inserts next list number`() {
        val oldText = "1. Hello\nSome text"

        val insertionIndex = 8
        val newText = buildString {
            append(oldText.substring(0, insertionIndex))
            append("\n")
            append(oldText.substring(insertionIndex))
        }

        val cursor = insertionIndex + 1
        val newValue = TextFieldValue(
            text = newText,
            selection = TextRange(cursor, cursor)
        )

        val result = continueListIfApplicable(oldText, newValue)

        val expectedText = "1. Hello\n2. \nSome text"
        assertEquals(expectedText, result.text)
        assertEquals(12, result.selection.start)
        assertEquals(12, result.selection.end)
    }

    @Test
    fun `test WHEN typing newline in the middle after an unordered list line THEN next line inserts dash prefix and user types more text`() {

        val oldText = "- Hello\nSome text"

        val insertionIndex = 7
        val intermediateText = buildString {
            append(oldText.substring(0, insertionIndex))
            append("\n")
            append(oldText.substring(insertionIndex))
        }

        val cursorAfterNewline = insertionIndex + 1
        val newValue = TextFieldValue(
            text = intermediateText,
            selection = TextRange(cursorAfterNewline, cursorAfterNewline)
        )

        val result = continueListIfApplicable(oldText, newValue)
        val expectedAfterAutoInsert = "- Hello\n- \nSome text"
        assertEquals(expectedAfterAutoInsert, result.text)

        assertEquals(10, result.selection.start)
        assertEquals(10, result.selection.end)

        val typedString = "next"
        val finalText = buildString {
            append(result.text.substring(0, result.selection.start))
            append(typedString)
            append(result.text.substring(result.selection.start))
        }

        val finalCursor = result.selection.start + typedString.length
        val finalValue = TextFieldValue(
            text = finalText,
            selection = TextRange(finalCursor, finalCursor)
        )

        val expectedFinalText = "- Hello\n- next\nSome text"
        assertEquals(expectedFinalText, finalValue.text)
        assertEquals(14, finalValue.selection.start)
        assertEquals(14, finalValue.selection.end)
    }

    @Test
    fun `test WHEN revisiting an empty unordered list item THEN no repeated prefix is inserted`() {
        // Initial text with an empty list item
        val oldText = "- Test\n- \nNew text"

        // Simulate the user placing the cursor in the empty list item and typing 'Text'
        val newText = "- Test\n- Text\nNew text"
        val cursor = newText.length  // Cursor should be after 'Text'

        val newValue = TextFieldValue(
            text = newText,
            selection = TextRange(cursor, cursor)
        )

        val result = continueListIfApplicable(oldText, newValue)

        // Expect the text to remain as is without inserting additional dashes
        val expectedText = "- Test\n- Text\nNew text"

        // Check text and cursor position
        assertEquals(expectedText, result.text)
        assertEquals(22, result.selection.start)  // Cursor at the end of 'Text'
        assertEquals(22, result.selection.end)
    }

    @Test
    fun `test WHEN revisiting an empty unordered list item after multiple newlines THEN no repeated prefix is inserted`() {
        // Initial text: user created an empty list item and hit Enter twice to create extra newlines
        val oldText = "- Test\n- \n\n"

        // Simulate the user moving back to the empty list item and typing 'Hello'
        val newText = "- Test\n- Hello\n\n"
        val cursor = newText.length  // Cursor at the end of 'Hello'

        val newValue = TextFieldValue(
            text = newText,
            selection = TextRange(cursor, cursor)
        )

        // Run the utility function
        val result = continueListIfApplicable(oldText, newValue)

        // Expect the text to remain as is without inserting repeated dashes
        val expectedText = "- Test\n- Hello\n\n"

        // Check text and cursor position
        assertEquals(expectedText, result.text)
        assertEquals(16, result.selection.start)  // Cursor at the end of 'Hello'
        assertEquals(16, result.selection.end)
    }

    @Test
    fun `test WHEN revisiting an empty ordered list item THEN no repeated prefix is inserted`() {
        // Initial text with an empty ordered list item
        val oldText = "1. Test\n2. \nMore text"

        // Simulate the user placing the cursor in the empty list item and typing 'Text'
        val newText = "1. Test\n2. Text\nMore text"
        val cursor = newText.length  // Cursor should be after 'Text'

        val newValue = TextFieldValue(
            text = newText,
            selection = TextRange(cursor, cursor)
        )

        // Run the utility function
        val result = continueListIfApplicable(oldText, newValue)

        // Expect the text to remain as is without inserting repeated prefixes
        val expectedText = "1. Test\n2. Text\nMore text"

        // Check text and cursor position
        assertEquals(expectedText, result.text)
        assertEquals(expectedText.length, result.selection.start)  // Cursor at the end of 'Text'
        assertEquals(expectedText.length, result.selection.end)
    }

    @Test
    fun `test WHEN revisiting an empty ordered list item after multiple newlines THEN no repeated prefix is inserted`() {
        // Initial text with an empty ordered list item and extra newlines
        val oldText = "1. Test\n2. \n\n"

        // Simulate the user moving back to the empty list item and typing 'Hello'
        val newText = "1. Test\n2. Hello\n\n"
        val cursor = newText.length  // Cursor at the end of 'Hello'

        val newValue = TextFieldValue(
            text = newText,
            selection = TextRange(cursor, cursor)
        )

        // Run the utility function
        val result = continueListIfApplicable(oldText, newValue)

        // Expect the text to remain as is without inserting repeated prefixes
        val expectedText = "1. Test\n2. Hello\n\n"

        // Check text and cursor position
        assertEquals(expectedText, result.text)
        assertEquals(expectedText.length, result.selection.start)  // Cursor at the end of 'Hello'
        assertEquals(expectedText.length, result.selection.end)
    }

}


