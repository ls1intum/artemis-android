package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/** Inserts composer content at the current selection. */
fun TextFieldValue.appendAtCursor(toInsert: String): TextFieldValue {
    val newText = text.replaceRange(selection.min, selection.max, toInsert)

    return TextFieldValue(
        text = newText,
        selection = TextRange(selection.min + toInsert.length)
    )
}
