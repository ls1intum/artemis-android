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

        val newValue = oldText.insertNewLineAfter("Hello").toTextFieldValue()

        val result = continueListIfApplicable(oldText, newValue)

        val expectedText = "- Hello\n- "
        assertEquals(expectedText, result.text)
        result.assertCursorAfterSubstring("\n- ")
    }

    @Test
    fun `test WHEN typing newline after an ordered list line THEN next line increments the list number`() {
        val oldText = "1. Hello"

        val newValue = oldText.insertNewLineAfter("Hello").toTextFieldValue()

        val result = continueListIfApplicable(oldText, newValue)

        val expectedText = "1. Hello\n2. "
        assertEquals(expectedText, result.text)
        result.assertCursorAfterSubstring("2. ")
    }

    @Test
    fun `test WHEN newline after non-list line THEN no prefix is inserted`() {
        val oldText = "Hello"

        val newValue = oldText.insertNewLineAfter("Hello").toTextFieldValue()

        val result = continueListIfApplicable(oldText, newValue)

        val expectedText = "Hello\n"
        assertEquals(expectedText, result.text)
        result.assertCursorAfterSubstring("\n")
    }

    @Test
    fun `test WHEN typing newline in the middle of multi-line text after ordered list THEN next line inserts next list number`() {
        val oldText = "1. Hello\nSome text"

        val newValue = oldText.insertNewLineAfter("Hello").toTextFieldValue()

        val result = continueListIfApplicable(oldText, newValue)

        val expectedText = "1. Hello\n2. \nSome text"
        assertEquals(expectedText, result.text)
        result.assertCursorAfterSubstring("2. ")
    }

    @Test
    fun `test WHEN typing newline in the middle after an unordered list line THEN next line inserts dash prefix and user types more text`() {
        val oldText = "- Hello\nSome text"

        val newValue = oldText.insertNewLineAfter("Hello").toTextFieldValue()

        val result = continueListIfApplicable(oldText, newValue)
        val expectedAfterAutoInsert = "- Hello\n- \nSome text"
        assertEquals(expectedAfterAutoInsert, result.text)

        result.assertCursorAfterSubstring("- Hello\n- ")

        val finalResult = result.text.insertAfterSubstring(
            substring = "\n- ",
            insertedText = "next"
        ).toTextFieldValue()

        val expectedFinalText = "- Hello\n- next\nSome text"
        assertEquals(expectedFinalText, finalResult.text)
        finalResult.assertCursorAfterSubstring("next")
    }

    @Test
    fun `test WHEN revisiting an empty unordered list item THEN no repeated prefix is inserted`() {
        // Initial text with an empty list item
        val oldText = "- Test\n- \nNew text"

        // Simulate the user placing the cursor in the empty list item and typing 'Text'
        val newValue = oldText.insertAfterSubstring(
            substring = "- Test\n- ",
            insertedText = "Text"
        ).toTextFieldValue()

        val result = continueListIfApplicable(oldText, newValue)

        // Expect the text to remain as is without inserting additional dashes
        val expectedText = "- Test\n- Text\nNew text"

        // Check text and cursor position
        assertEquals(expectedText, result.text)
        result.assertCursorAfterSubstring("Text")
    }

    @Test
    fun `test WHEN revisiting an empty unordered list item after multiple newlines THEN no repeated prefix is inserted`() {
        // Initial text: user created an empty list item and hit Enter twice to create extra newlines
        val oldText = "- Test\n- \n\n"

        // Simulate the user moving back to the empty list item and typing 'Hello'
        val newValue = oldText.insertAfterSubstring(
            substring = "- Test\n- ",
            insertedText = "Hello"
        ).toTextFieldValue()

        // Run the utility function
        val result = continueListIfApplicable(oldText, newValue)

        // Expect the text to remain as is without inserting repeated dashes
        val expectedText = "- Test\n- Hello\n\n"

        // Check text and cursor position
        assertEquals(expectedText, result.text)
        result.assertCursorAfterSubstring("Hello")
    }

    @Test
    fun `test WHEN revisiting an empty ordered list item THEN no repeated prefix is inserted`() {
        // Initial text with an empty ordered list item
        val oldText = "1. Test\n2. \nMore text"

        // Simulate the user placing the cursor in the empty list item and typing 'Text'
        val newValue = oldText.insertAfterSubstring(
            substring = "2. ",
            insertedText = "Text"
        ).toTextFieldValue()

        // Run the utility function
        val result = continueListIfApplicable(oldText, newValue)

        // Expect the text to remain as is without inserting repeated prefixes
        val expectedText = "1. Test\n2. Text\nMore text"

        // Check text and cursor position
        assertEquals(expectedText, result.text)
        result.assertCursorAfterSubstring("Text")
    }

    @Test
    fun `test WHEN revisiting an empty ordered list item after multiple newlines THEN no repeated prefix is inserted`() {
        // Initial text with an empty ordered list item and extra newlines
        val oldText = "1. Test\n2. \n\n"

        // Simulate the user moving back to the empty list item and typing 'Hello'
        val newValue = oldText.insertAfterSubstring(
            substring = "2. ",
            insertedText = "Hello"
        ).toTextFieldValue()

        // Run the utility function
        val result = continueListIfApplicable(oldText, newValue)

        // Expect the text to remain as is without inserting repeated prefixes
        val expectedText = "1. Test\n2. Hello\n\n"

        // Check text and cursor position
        assertEquals(expectedText, result.text)
        result.assertCursorAfterSubstring("Hello")
    }


    private fun String.insertNewLineAfter(substring: String): InsertionResult = insertAfterSubstring(
        substring = substring,
        insertedText = "\n"
    )

    private fun String.insertAfterSubstring(
        substring: String,
        insertedText: String
    ): InsertionResult {
        val firstIndex = indexOf(substring)
        if (firstIndex == -1) {
            throw IllegalArgumentException("Substring not found in the original string")
        }

        val insertionIndex = firstIndex + substring.length
        return insertAtIndex(insertionIndex, insertedText)
    }

    private fun String.insertAtIndex(index: Int, insertedText: String): InsertionResult {
        val newString = buildString {
            append(this@insertAtIndex.substring(0, index))
            append(insertedText)
            append(this@insertAtIndex.substring(index))
        }
        return InsertionResult(newString, index + insertedText.length)
    }

    private data class InsertionResult(
        val newString: String,
        val cursor: Int
    ) {
        fun toTextFieldValue() = TextFieldValue(
            text = newString,
            selection = TextRange(cursor, cursor)
        )
    }

    private fun TextFieldValue.assertCursorAfterSubstring(substring: String) {
        val firstIndex = text.indexOf(substring)
        if (firstIndex == -1) {
            throw IllegalArgumentException("Substring not found in the original string")
        }

        val expectedCursor = firstIndex + substring.length
        assertEquals(expectedCursor, selection.start)
        assertEquals(expectedCursor, selection.end)
    }
}


