package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.autocomplete.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.getTextBeforeSelection
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.autocomplete.AutoCompleteHintCollection
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.autocomplete.AutoCompleteType
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.autocomplete.LocalReplyAutoCompleteHintProvider
import kotlinx.coroutines.flow.Flow

internal object AutoCompleteUtil {

    fun perform(
        textFieldValue: TextFieldValue,
        tagChars: List<Char>,
        replacement: String
    ): TextFieldValue {
        // Perform replace in text
        val replacementStart = textFieldValue
            .getTextBeforeSelection(Int.MAX_VALUE)
            .indexOfLastWhileTag(tagChars)
            ?: return textFieldValue

        val replacementEnd = textFieldValue.selection.min

        val newText = textFieldValue
            .text
            .replaceRange(replacementStart, replacementEnd, replacement)

        return textFieldValue.copy(
            text = newText,
            // Put cursor after replacement.
            selection = TextRange(replacementStart + replacement.length)
        )
    }

    /**
     * @return a list of auto complete hints that should be displayed, or null if no auto complete hints are to be displayed.
     */
    @Composable
    fun manageAutoCompleteHints(
        textFieldValue: TextFieldValue,
        requestedAutocompleteType: AutoCompleteType? = null
    ): List<AutoCompleteHintCollection>? {
        val replyAutoCompleteHintProvider = LocalReplyAutoCompleteHintProvider.current
        var replyAutoCompleteHintProducer: Flow<DataState<List<AutoCompleteHintCollection>>>? by remember {
            mutableStateOf(
                null
            )
        }

        val tagChars = replyAutoCompleteHintProvider.legalTagChars
        LaunchedEffect(textFieldValue, replyAutoCompleteHintProvider) {
            // Check that no text is selected, and instead we have a simple cursor
            replyAutoCompleteHintProducer =
                textFieldValue.getAutoCompleteReplacementTextFirstIndex(tagChars)?.let { tagIndex ->
                    val tagChar = textFieldValue.text[tagIndex]
                    val replacementWord = if (textFieldValue.text.length > tagIndex + 1) {
                        textFieldValue.text.substring(tagIndex + 1).takeWhileTag()
                    } else ""

                    replyAutoCompleteHintProvider.produceAutoCompleteHints(tagChar, replacementWord)
                }
        }

        val replyAutoCompleteHints = replyAutoCompleteHintProducer
            ?.collectAsState(DataState.Loading())
            ?.value
            ?.orElse(emptyList())
            .orEmpty()
            .toMutableList()
            .apply {
                requestedAutocompleteType?.let { type ->
                    retainAll { it.type == type }
                }
            }

        var latestValidAutoCompleteHints: List<AutoCompleteHintCollection>? by remember {
            mutableStateOf(null)
        }

        LaunchedEffect(replyAutoCompleteHintProducer, replyAutoCompleteHints) {
            if (replyAutoCompleteHintProducer == null) {
                latestValidAutoCompleteHints = null
            } else if (replyAutoCompleteHints.isNotEmpty()) {
                latestValidAutoCompleteHints = replyAutoCompleteHints
            }
        }

        return latestValidAutoCompleteHints
    }

    private fun CharSequence.indexOfLastWhileTag(tagChars: List<Char>): Int? {
        var foundWhitespace = false

        for (index in lastIndex downTo 0) {
            val currentChar = this[index]

            if (currentChar.isWhitespace() && foundWhitespace) {
                return index + 1
            } else if (currentChar in tagChars) {
                return index
            } else if (currentChar.isWhitespace()) {
                foundWhitespace = true
            }
        }

        return if (isEmpty()) null else 0
    }


    /**
     * @return the index of the tagging char of the current replacement text, or null if the user is currently
     * not entering a replaceable text.
     */
    private fun TextFieldValue.getAutoCompleteReplacementTextFirstIndex(tagChars: List<Char>): Int? {
        return if (selection.collapsed) {
            // Gather the last word, meaning the characters from the last whitespace until the cursor.
            val tagCharIndex = getTextBeforeSelection(Int.MAX_VALUE)
                .indexOfLastWhileTag(tagChars)
                ?: return null

            val tagChar = text[tagCharIndex]

            if (tagChar in tagChars) tagCharIndex else null
        } else null
    }


    /**
     * Takes characters until the end of the string or a second whitespace has been found
     */
    private fun String.takeWhileTag(): String {
        var foundWhitespace = false

        for (index in indices) {
            val currentChar = this[index]

            if (currentChar.isWhitespace() && foundWhitespace) {
                return substring(0, index)
            } else if (currentChar.isWhitespace()) {
                foundWhitespace = true
            }
        }

        return this
    }
}