package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.foundation.layout.width
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.text.TextRange
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsNotSelected
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.BaseChatUITest
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class MessageComposerUiTest : BaseChatUITest() {
    @Test
    fun `code blocks keep block controls available and disable inline insertions`() {
        setupReplyTextField(initialText = "```\ncode\n```")
        composeTestRule.onNodeWithContentDescription("Show formatting").performClick()
        composeTestRule.onNodeWithContentDescription("Bold").assertIsNotEnabled()
        composeTestRule.onNodeWithContentDescription("Link").assertIsNotEnabled()
        composeTestRule.onNodeWithContentDescription("Hide formatting").performClick()
        composeTestRule.onNodeWithContentDescription("Add attachment or reference").assertIsNotEnabled()
    }

    @Test
    fun `channel action opens completion directly and course references live under add`() {
        setupReplyTextField()
        val field = composeTestRule.onNodeWithTag(TEST_TAG_MARKDOWN_TEXTFIELD)
        composeTestRule.onNodeWithContentDescription("Mention a channel").performClick()
        field.assertTextEquals("#")
        field.performTextClearance()
        composeTestRule.onNodeWithContentDescription("Add attachment or reference").performClick()
        composeTestRule.onNodeWithText("Exercises").assertIsDisplayed().performClick()
        field.assertTextEquals("#")
    }

    @Test
    fun `add and send stay reachable after scrolling quick actions in a narrow window`() {
        setupReplyTextField(Modifier.width(220.dp))
        composeTestRule.onNode(hasScrollAction() and hasAnyDescendant(hasContentDescription("Show formatting")))
            .performTouchInput { swipeLeft() }
        composeTestRule.onNodeWithContentDescription("Add attachment or reference").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithText("Upload Attachment").assertIsDisplayed()
        composeTestRule.onNodeWithTag(TEST_TAG_REPLY_SEND_BUTTON).assertIsDisplayed()
    }

    @Test
    fun `native copy and paste retain styles and Artemis reference payloads`() {
        var source = ""
        setupReplyTextField(initialText = "**hello** [user]Ada(ada)[/user]", onSourceChanged = { source = it.text })
        val field = composeTestRule.onNodeWithTag(TEST_TAG_MARKDOWN_TEXTFIELD)
        field.performClick().performTextInputSelection(TextRange(0, 9))
        field.performSemanticsAction(SemanticsActions.CopyText) { it() }
        field.performTextClearance()
        field.performSemanticsAction(SemanticsActions.PasteText) { it() }
        field.assertTextEquals("hello Ada")
        composeTestRule.runOnIdle { org.junit.Assert.assertTrue(source, source.contains("[user]Ada(ada)[/user]")) }
        field.performTextInputSelection(TextRange(0, 5))
        composeTestRule.onNodeWithContentDescription("Show formatting").performClick()
        composeTestRule.onNodeWithContentDescription("Bold").assertIsSelected()
    }

    @Test
    fun `external Markdown paste hydrates formatting while ordinary typing remains literal`() {
        setupReplyTextField()
        val field = composeTestRule.onNodeWithTag(TEST_TAG_MARKDOWN_TEXTFIELD)
        field.performClick()
        composeTestRule.runOnIdle {
            val clipboard = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Test", "**world**"))
        }
        field.performSemanticsAction(SemanticsActions.PasteText) { it() }
        field.assertTextEquals("world")
        field.performTextInputSelection(TextRange(0, 5))
        composeTestRule.onNodeWithContentDescription("Show formatting").performClick()
        composeTestRule.onNodeWithContentDescription("Bold").assertIsSelected()
        field.performTextClearance()
        field.performTextInput("**literal**")
        field.assertTextEquals("**literal**")
    }

    @Test
    fun `pasting inside code keeps Markdown literal`() {
        setupReplyTextField(initialText = "`code`")
        val field = composeTestRule.onNodeWithTag(TEST_TAG_MARKDOWN_TEXTFIELD)
        field.performClick().performTextInputSelection(TextRange(4))
        composeTestRule.runOnIdle {
            val clipboard = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Test", "**literal**"))
        }
        field.performSemanticsAction(SemanticsActions.PasteText) { it() }
        field.assertTextEquals("code**literal**")
    }

    @Test
    fun `native copy cut undo and redo use visible text and preserve marks`() {
        setupReplyTextField(initialText = "**hello** world")
        val field = composeTestRule.onNodeWithTag(TEST_TAG_MARKDOWN_TEXTFIELD)
        field.performClick().performTextInputSelection(TextRange(1, 8))
        field.performSemanticsAction(SemanticsActions.CopyText) { it() }
        composeTestRule.runOnIdle {
            val clipboard = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            org.junit.Assert.assertEquals("ello wo", clipboard.primaryClip?.getItemAt(0)?.text?.toString())
        }
        field.performTextInputSelection(TextRange(1, 8))
        field.performSemanticsAction(SemanticsActions.CutText) { it() }
        field.assertTextEquals("hrld")
        field.performKeyInput { keyDown(Key.CtrlLeft); pressKey(Key.Z); keyUp(Key.CtrlLeft) }
        field.assertTextEquals("hello world")
        field.performKeyInput { keyDown(Key.CtrlLeft); pressKey(Key.Y); keyUp(Key.CtrlLeft) }
        field.assertTextEquals("hrld")
    }

    @Test
    fun `undo and redo restore a toolbar formatting change with unchanged visible text`() {
        setupReplyTextField(initialText = "Hello")
        val field = composeTestRule.onNodeWithTag(TEST_TAG_MARKDOWN_TEXTFIELD)
        field.performClick().performTextInputSelection(TextRange(0, 5))
        composeTestRule.onNodeWithContentDescription("Show formatting").performClick()
        composeTestRule.onNodeWithContentDescription("Bold").performClick().assertIsSelected()
        field.performKeyInput { keyDown(Key.CtrlLeft); pressKey(Key.Z); keyUp(Key.CtrlLeft) }
        composeTestRule.onNodeWithContentDescription("Bold").assertIsNotSelected()
        field.performKeyInput { keyDown(Key.CtrlLeft); pressKey(Key.Y); keyUp(Key.CtrlLeft) }
        composeTestRule.onNodeWithContentDescription("Bold").assertIsSelected()
    }
    @Test
    fun `long draft keeps send reachable when composer height is constrained`() {
        setupReplyTextField(Modifier.fillMaxWidth().heightIn(max = 160.dp))
        composeTestRule.onNodeWithTag(TEST_TAG_MARKDOWN_TEXTFIELD).performTextInput((1..20).joinToString("\n") { "Line $it" })
        composeTestRule.onNodeWithTag(TEST_TAG_REPLY_SEND_BUTTON).assertIsDisplayed().assertIsEnabled()
        composeTestRule.onNodeWithContentDescription("Show formatting").assertIsDisplayed()
    }
    @Test
    fun `mention action replaces selected text`() {
        setupReplyTextField()
        composeTestRule.onNodeWithTag(TEST_TAG_MARKDOWN_TEXTFIELD).performTextInput("Hello world")
        composeTestRule.onNodeWithTag(TEST_TAG_MARKDOWN_TEXTFIELD).performTextInputSelection(TextRange(11, 6))
        composeTestRule.onNodeWithContentDescription("Mention a member").performClick()
        composeTestRule.onNodeWithTag(TEST_TAG_MARKDOWN_TEXTFIELD).assertTextEquals("Hello @")
    }
    @Test
    fun `empty composer offers attachments formatting and a disabled send`() {
        setupReplyTextField()
        composeTestRule.onNodeWithText("Message TestChat").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Add attachment or reference").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithText("Upload Image").assertIsDisplayed()
        composeTestRule.onNodeWithText("Upload Attachment").assertIsDisplayed()
        composeTestRule.onNodeWithTag(TEST_TAG_REPLY_SEND_BUTTON).assertIsNotEnabled()
    }

    @Test
    fun `formatting replaces quick actions and send remains available`() {
        setupReplyTextField()
        composeTestRule.onNodeWithTag(TEST_TAG_MARKDOWN_TEXTFIELD).performTextInput("Hello")
        composeTestRule.onNodeWithContentDescription("Show formatting").performClick()
        composeTestRule.onNodeWithContentDescription("Bold").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Add attachment or reference").assertDoesNotExist()
        composeTestRule.onNodeWithTag(TEST_TAG_REPLY_SEND_BUTTON).assertIsDisplayed().assertIsEnabled()
        composeTestRule.onNodeWithContentDescription("Hide formatting").performClick()
        composeTestRule.onNodeWithContentDescription("Add attachment or reference").assertIsDisplayed()
    }

    @Test
    fun `empty formatting keeps send disabled until content is typed`() {
        setupReplyTextField()
        composeTestRule.onNodeWithContentDescription("Show formatting").performClick()
        composeTestRule.onNodeWithContentDescription("Bold").performClick()
        composeTestRule.onNodeWithTag(TEST_TAG_REPLY_SEND_BUTTON).assertIsNotEnabled()
        composeTestRule.onNodeWithTag(TEST_TAG_MARKDOWN_TEXTFIELD).performTextInput("Hello")
        composeTestRule.onNodeWithTag(TEST_TAG_REPLY_SEND_BUTTON).assertIsEnabled()
    }
}
