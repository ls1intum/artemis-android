package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import android.view.inputmethod.InputConnection
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.MarkdownEditEngine
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class ComposerInputConnectionTest {
    @Test fun `software keyboard Backspace at first quote body removes format without invisible text`() {
        var source = TextFieldValue("> quote", TextRange(2))
        val native = mockk<InputConnection>(relaxed = true)
        val connection = ComposerInputConnection(native) {
            MarkdownEditEngine.removeBlockAtStart(source)?.let { source = it; true } ?: false
        }
        assertTrue(connection.deleteSurroundingText(1, 0))
        assertEquals("quote", source.text)
        verify(exactly = 0) { native.deleteSurroundingText(any(), any()) }
    }

    @Test fun `ordinary software deletion delegates to native editing`() {
        val native = mockk<InputConnection>(relaxed = true)
        val connection = ComposerInputConnection(native) { false }
        connection.deleteSurroundingTextInCodePoints(1, 0)
        verify(exactly = 1) { native.deleteSurroundingTextInCodePoints(1, 0) }
    }
}
