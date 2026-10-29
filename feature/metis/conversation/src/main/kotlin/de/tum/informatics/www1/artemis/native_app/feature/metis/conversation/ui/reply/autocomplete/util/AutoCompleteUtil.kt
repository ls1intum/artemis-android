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
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.MarkdownStyle
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.autocomplete.AutoCompleteHintCollection
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.autocomplete.AutoCompleteType
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.autocomplete.LocalReplyAutoCompleteHintProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.MarkdownPresentation
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.MarkdownEditEngine
import kotlinx.coroutines.flow.Flow

internal object AutoCompleteUtil {

    data class Query(val sourceStart: Int, val sourceEnd: Int, val tag: Char, val text: String)

    fun query(value: TextFieldValue, tagChars: List<Char>): Query? {
        if (!value.selection.collapsed) return null
        val presentation = MarkdownPresentation.parse(value.text, value.selection)
        if (presentation.spans.any { it.style in setOf(MarkdownStyle.InlineCode, MarkdownStyle.CodeBlock) && value.selection.start in it.contentStart..it.contentEnd }) return null
        val end = presentation.offsetMapping.originalToTransformed(value.selection.start)
        val prefix = presentation.text.substring(0, end)
        val start = prefix.indexOfLastWhileTag(tagChars) ?: return null
        if (prefix.getOrNull(start) !in tagChars) return null
        val sourceStart = presentation.offsetMapping.transformedToOriginal(start)
        var sourceEnd = value.selection.start
        while (sourceEnd > sourceStart && presentation.offsetMapping.originalToTransformed(sourceEnd - 1) == end) sourceEnd--
        for (span in presentation.spans.filter { it.start >= sourceStart && it.start < sourceEnd }) {
            // Include complete inner styles, while retaining styles that surround the mention.
            if (span.contentEnd > sourceEnd) return null
            sourceEnd = maxOf(sourceEnd, span.end)
        }
        return Query(sourceStart, sourceEnd, prefix[start], prefix.substring(start + 1))
    }

    fun perform(
        textFieldValue: TextFieldValue,
        tagChars: List<Char>,
        replacement: String
    ): TextFieldValue {
        // Perform replace in text
        val query = query(textFieldValue, tagChars) ?: return textFieldValue
        val replacementStart = query.sourceStart
        val replacementEnd = query.sourceEnd

        return MarkdownEditEngine.insertMarkup(textFieldValue.copy(selection = TextRange(replacementStart, replacementEnd)), replacement)
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
                query(textFieldValue, tagChars)?.let { query ->
                    replyAutoCompleteHintProvider.produceAutoCompleteHints(query.tag, query.text)
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


}
