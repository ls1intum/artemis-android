package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.assertCursorAfterSubstring
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

        val finalValue = result.text.insertAfterSubstring(
            substring = "\n- ",
            insertedText = "next"
        ).toTextFieldValue()

        val finalResult = continueListIfApplicable(result.text, finalValue)
        val expectedFinalText = "- Hello\n- next\nSome text"
        assertEquals(expectedFinalText, finalResult.text)
        finalResult.assertCursorAfterSubstring("next")
    }

    @Test
    fun `test GIVEN a list with an empty item followed by a newline WHEN revisiting the empty list item and typing THEN no repeated prefix is inserted`() {
        // https://github.com/ls1intum/artemis-android/issues/342

        // Initial text with an empty ordered list item
        val oldText = "1. abc\n2. \n"

        // User typing "d"
        val userInsert1 = oldText.insertAfterSubstring(
            substring = "2. ",
            insertedText = "d"
        ).toTextFieldValue()

        val intermediateResult = continueListIfApplicable(oldText, userInsert1)

        val expectedText1 = "1. abc\n2. d\n"
        assertEquals(expectedText1, intermediateResult.text)
        intermediateResult.assertCursorAfterSubstring("d")

        // User typing "e"
        val userInsert2 = intermediateResult.text.insertAfterSubstring(
            substring = "d",
            insertedText = "e"
        ).toTextFieldValue()

        val result = continueListIfApplicable(intermediateResult.text, userInsert2)

        val expectedText = "1. abc\n2. de\n"
        assertEquals(expectedText, result.text)
        result.assertCursorAfterSubstring("e")
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
}


