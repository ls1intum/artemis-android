package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.MarkdownStyle

internal object MarkdownStyleUtil {

    fun apply(
        style: MarkdownStyle,
        currentTextFieldValue: TextFieldValue
    ): TextFieldValue {
        val selection = currentTextFieldValue.selection
        val text = currentTextFieldValue.text

        val startTag = style.startTag
        val endTag = style.endTag

        if (selection.collapsed) {
            // No text selected
            return applyAtCursorPosition(
                style = style,
                text = text,
                selection = selection,
                startTag = startTag,
                endTag = endTag
            )
        } else {
            return applyOnSelectedTextRange(
                text = text,
                selection = selection,
                style = style,
                startTag = startTag,
                endTag = endTag
            )
        }
    }

    private fun applyAtCursorPosition(
        style: MarkdownStyle,
        text: String,
        selection: TextRange,
        startTag: String,
        endTag: String
    ): TextFieldValue {
        if (style == MarkdownStyle.CodeBlock) {
            // Insert code block with newlines
            val newText = text.substring(0, selection.start) +
                    "$startTag\n\n$endTag" +
                    text.substring(selection.end)
            val newCursorPosition = selection.start + startTag.length + 1

            return TextFieldValue(
                text = newText,
                selection = TextRange(newCursorPosition, newCursorPosition)
            )
        } else {
            // Other styles
            val newText = text.substring(
                0,
                selection.start
            ) + startTag + endTag + text.substring(selection.end)
            val newCursorPosition = selection.start + startTag.length

            return TextFieldValue(
                text = newText,
                selection = TextRange(newCursorPosition, newCursorPosition)
            )
        }
    }

    private fun applyOnSelectedTextRange(
        text: String,
        selection: TextRange,
        style: MarkdownStyle,
        startTag: String,
        endTag: String
    ): TextFieldValue {
        val selectedText = text.substring(selection.start, selection.end)
        if (style == MarkdownStyle.CodeBlock) {
            val newText = text.substring(0, selection.start) +
                    "$startTag\n$selectedText\n$endTag" +
                    text.substring(selection.end)
            val newSelection = TextRange(
                selection.start + startTag.length + 1,
                selection.end + startTag.length + 1
            )

            return TextFieldValue(
                text = newText,
                selection = newSelection
            )
        } else {
            val newText = text.substring(0, selection.start) +
                    startTag +
                    selectedText +
                    endTag +
                    text.substring(selection.end)
            val newSelection = TextRange(selection.end + startTag.length + endTag.length)

            return TextFieldValue(
                text = newText,
                selection = newSelection
            )
        }
    }

}