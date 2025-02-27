package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation

import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals


internal fun TextFieldValue.assertCursorAfterSubstring(substring: String) {
    val firstIndex = text.indexOf(substring)
    if (firstIndex == -1) {
        throw IllegalArgumentException("Substring not found in the original string")
    }

    val expectedCursor = firstIndex + substring.length
    assertEquals(expectedCursor, selection.start)
    assertEquals(expectedCursor, selection.end)
}