package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.BaseChatUITest
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class ReplyTextFieldVisibilityUITest : BaseChatUITest() {

    @Test
    fun `test GIVEN the thread view is shown containing one post and three answer posts WHEN the markdown text field is clicked THEN the keyboard is shown below the markdown text field`() {
        setupThreadUi(posts[0])
        runTest()
    }

    @Test
    fun `test GIVEN the chat list containing three posts is shown WHEN the markdown text field is clicked THEN the keyboard is shown below the markdown text field`() {
        setupChatUi(posts)
        runTest()
    }

    private fun runTest() {
        val markdownTextField = composeTestRule.onNodeWithTag(TEST_TAG_CAN_CREATE_REPLY)
        val initialPosition = markdownTextField.fetchSemanticsNode().positionInRoot.y

        markdownTextField.performClick()
        composeTestRule.waitForIdle()

        val newPosition = markdownTextField.fetchSemanticsNode().positionInRoot.y

        markdownTextField
            .assertExists()
            .assertIsDisplayed()
        assertTrue("Text field should move up when the keyboard appears", newPosition < initialPosition)
    }
}
