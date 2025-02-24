package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue


/**
 * If the previous line starts with "-", continue as unordered.
 * If it starts with "N. ", continue as ordered.
 * Otherwise, do nothing (return the original newValue).
 */
object MarkdownListContinuationUtil {
    fun continueListIfApplicable(
        oldText: String,
        newValue: TextFieldValue
    ): TextFieldValue {
        val afterUnordered = continueUnorderedList(oldText, newValue)
        if (afterUnordered != newValue) {
            return afterUnordered  // Unordered insertion happened
        }

        val afterOrdered = continueOrderedList(oldText, newValue)
        if (afterOrdered != newValue) {
            return afterOrdered  // Ordered insertion happened
        }

        return newValue
    }

    private fun continueUnorderedList(oldValue: String, newValue: TextFieldValue): TextFieldValue {
        return continueList(oldValue, newValue) { line ->
            if (line.startsWith("- ")) "- " else null
        }
    }

    private fun continueOrderedList(oldValue: String, newValue: TextFieldValue): TextFieldValue {
        return continueList(oldValue, newValue) { line ->
            val match = Regex("""^(\d+)\.\s""").find(line) ?: return@continueList null
            val number = match.groupValues[1].toIntOrNull() ?: return@continueList null
            "${number + 1}. "
        }
    }

    /**
     * Shared logic for continuing a list (ordered or unordered).
     *
     * @param oldValue the old text (String)
     * @param newValue the new TextFieldValue
     * @param findNextPrefix a function that inspects the current line (after trimming)
     * and returns the next prefix if we want to continue the list, or null if not.
     */
    private fun continueList(
        oldValue: String,
        newValue: TextFieldValue,
        findNextPrefix: (previousLine: String) -> String?
    ): TextFieldValue {
        val newText = newValue.text
        val cursor = newValue.selection.start

        if (newText.length == oldValue.length + 1) {
            if (cursor > 0 && newText[cursor - 1] == '\n') {
                if (cursor > 1 && newText[cursor - 2] == '\n') {
                    return newValue
                }

                val prevLineBreakIndex = newText.lastIndexOf('\n', startIndex = cursor - 2)
                val lineStart = if (prevLineBreakIndex == -1) 0 else (prevLineBreakIndex + 1)
                val previousLine = newText.substring(lineStart, cursor - 1)

                if (previousLine.isBlank()) {
                    return newValue
                }

                val prefix = findNextPrefix(previousLine.trim()) ?: return newValue

                val updatedText = buildString {
                    append(newText.substring(0, cursor))
                    append(prefix)
                    append(newText.substring(cursor))
                }
                val newCursor = cursor + prefix.length

                return newValue.copy(
                    text = updatedText,
                    selection = TextRange(newCursor, newCursor)
                )
            }
        }

        return newValue
    }
}