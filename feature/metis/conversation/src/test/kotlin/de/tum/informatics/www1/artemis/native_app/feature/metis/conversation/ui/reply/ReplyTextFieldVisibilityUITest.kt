package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.BaseChatUITest
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class ReplyTextFieldVisibilityUITest : BaseChatUITest() {

    @Test
    fun `focusing the thread composer keeps the editor and send action visible`() {
        setupThreadUi(posts[0])
        composeTestRule.onNodeWithText("Reply to thread").assertIsDisplayed()
        runTest()
    }

    @Test
    fun `focusing the chat composer keeps the editor and send action visible`() {
        setupChatUi(posts)
        runTest()
    }

    private fun runTest() {
        val markdownTextField = composeTestRule.onNodeWithTag(TEST_TAG_CAN_CREATE_REPLY)
        composeTestRule.onNodeWithTag(TEST_TAG_MARKDOWN_TEXTFIELD).performClick()
        composeTestRule.waitForIdle()

        markdownTextField
            .assertExists()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(TEST_TAG_MARKDOWN_TEXTFIELD).assertIsFocused().assertIsDisplayed()
        composeTestRule.onNodeWithTag(TEST_TAG_REPLY_SEND_BUTTON).assertIsDisplayed()
    }
}
