package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class ComposerAttachmentsUiTest : BaseComposeTest() {
    @Test
    fun `restored file and remote image expose accessible attachment actions without an image provider`() {
        val value = mutableStateOf(TextFieldValue("[report](/api/files/report.pdf) ![diagram](https://example.org/image.png)"))
        composeTestRule.setContent {
            MaterialTheme { ComposerAttachments(value.value, { value.value = it }) }
        }

        composeTestRule.onNodeWithContentDescription("Manage file attachment: report").performClick()
        composeTestRule.onNodeWithText("Remove attachment").performClick()
        composeTestRule.onNodeWithContentDescription("Manage image attachment: diagram").assertExists()
        composeTestRule.runOnIdle { assertEquals(" ![diagram](https://example.org/image.png)", value.value.text) }
    }

    @Test
    fun `renaming an attachment while another upload completes preserves both uploads`() {
        val value = mutableStateOf(TextFieldValue("[report](/api/files/report.pdf)"))
        composeTestRule.setContent {
            MaterialTheme { ComposerAttachments(value.value, { value.value = it }) }
        }
        composeTestRule.onNodeWithContentDescription("Manage file attachment: report").performClick()
        composeTestRule.onNodeWithText("Rename attachment").performClick()
        composeTestRule.onNodeWithText("Attachment name").performTextReplacement("Report [final] 😀")
        composeTestRule.runOnIdle {
            val source = value.value.text + "\n![new](/api/files/new.png)"
            value.value = TextFieldValue(source, TextRange(source.length))
        }
        composeTestRule.onNodeWithText("Rename", substring = false).performClick()

        composeTestRule.runOnIdle {
            assertEquals("[Report \\[final\\] 😀](/api/files/report.pdf)\n![new](/api/files/new.png)", value.value.text)
            assertEquals(TextRange(value.value.text.length), value.value.selection)
        }
    }

    @Test
    fun `stale dialog cannot rename a different restored draft`() {
        val value = mutableStateOf(TextFieldValue("[report](/api/files/report.pdf)"))
        composeTestRule.setContent {
            MaterialTheme { ComposerAttachments(value.value, { value.value = it }) }
        }
        composeTestRule.onNodeWithContentDescription("Manage file attachment: report").performClick()
        composeTestRule.onNodeWithText("Rename attachment").performClick()
        composeTestRule.onNodeWithText("Attachment name").performTextReplacement("Changed")
        composeTestRule.runOnIdle { value.value = TextFieldValue("Different draft") }

        composeTestRule.onNodeWithText("Rename", substring = false).assertIsNotEnabled()
        composeTestRule.onNodeWithText("This attachment has changed. Close this dialog and try again.").assertExists()
        composeTestRule.runOnIdle { assertEquals("Different draft", value.value.text) }
    }

    @Test
    fun `empty attachment names disable rename and cancelling preserves source`() {
        val original = TextFieldValue("[report](/api/files/report.pdf)")
        val value = mutableStateOf(original)
        composeTestRule.setContent {
            MaterialTheme { ComposerAttachments(value.value, { value.value = it }) }
        }
        composeTestRule.onNodeWithContentDescription("Manage file attachment: report").performClick()
        composeTestRule.onNodeWithText("Rename attachment").performClick()
        composeTestRule.onNodeWithText("Attachment name").performTextReplacement("   ")

        composeTestRule.onNodeWithText("Rename", substring = false).assertIsNotEnabled()
        composeTestRule.onNodeWithText("Cancel").performClick()
        composeTestRule.runOnIdle { assertEquals(original, value.value) }
    }
}
