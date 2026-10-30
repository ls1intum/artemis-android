package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class ComposerLinkDialogUiTest : BaseComposeTest() {
    @Test
    fun `opening and applying an existing link preserves its escaped label`() {
        val original = TextFieldValue("[A \\[B\\]](https://example.com)", TextRange(2))
        var result: TextFieldValue? = null
        composeTestRule.setContent {
            MaterialTheme {
                ComposerLinkDialog(original, original, onDismiss = {}, onConfirm = { result = it })
            }
        }
        composeTestRule.onNodeWithText("A [B]").assertExists()
        composeTestRule.onNodeWithText("Apply").performClick()
        composeTestRule.runOnIdle { assertEquals(original.text, result?.text) }
    }

    @Test
    fun `applying a link after a background upload keeps the attachment`() {
        val original = TextFieldValue("Read this", TextRange(5, 9))
        val current = mutableStateOf(original)
        var result: TextFieldValue? = null
        composeTestRule.setContent {
            MaterialTheme {
                ComposerLinkDialog(original, current.value, onDismiss = {}, onConfirm = { result = it })
            }
        }
        composeTestRule.onNodeWithText("Link address").performTextInput("https://example.com")
        composeTestRule.runOnIdle { current.value = TextFieldValue("Read this\n[file](/api/core/files/file.pdf)") }
        composeTestRule.onNodeWithText("Apply").performClick()
        composeTestRule.runOnIdle {
            assertEquals("Read [this](https://example.com)\n[file](/api/core/files/file.pdf)", result?.text)
        }
    }
}
