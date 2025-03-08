package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

fun TextFieldValue.appendAtCursor(toInsert: String): TextFieldValue {
    val newText = text.substring(0, selection.start) +
            toInsert +
            text.substring(selection.start)

    return TextFieldValue(
        text = newText,
        selection = TextRange(selection.start + toInsert.length)
    )
}