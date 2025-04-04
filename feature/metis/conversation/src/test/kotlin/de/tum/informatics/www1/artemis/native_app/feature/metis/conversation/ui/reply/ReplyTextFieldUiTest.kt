package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.BaseChatUITest
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.autocomplete.TEST_TAG_REPLY_AUTO_COMPLETE_POPUP_LIST
import org.junit.Before
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class ReplyTextFieldUiTest : BaseChatUITest() {

    @Before
    fun setUp() {
        setupReplyTextField()

        // Click the unfocused textField to focus and expand the textField
        composeTestRule.onNodeWithTag(TEST_TAG_UNFOCUSED_TEXT_FIELD).performClick()
    }

    @Test
    fun `test GIVEN an empty reply textField WHEN doing nothing THEN the autoCompletion dialog is hidden`() {
        composeTestRule.assertAllAutoCompletionHintsHidden()
    }

    @Test
    fun `test GIVEN an empty reply textField WHEN entering the tag character @ THEN a list of autoCompletionHints for users shows`() {
        composeTestRule.onNodeWithTag(TEST_TAG_MARKDOWN_TEXTFIELD).performTextInput("@")
        composeTestRule.assertAllAutoCompletionHintsShown()
    }

    @Test
    fun `test GIVEN the autoCompletion dialog WHEN clicking an entry THEN the replacement is inserted into the textField and the dialog is hidden`() {
        composeTestRule.onNodeWithTag(TEST_TAG_MARKDOWN_TEXTFIELD).performTextInput("@")

        composeTestRule.onNodeWithText("User1").performClick()

        composeTestRule.onNodeWithTag(TEST_TAG_MARKDOWN_TEXTFIELD).assertTextEquals("<User1>")
        composeTestRule.assertAllAutoCompletionHintsHidden()
    }

    @Test
    fun `test GIVEN the textField WHEN entering a non-tag character THEN the autoCompletion dialog is hidden`() {
        composeTestRule.onNodeWithTag(TEST_TAG_MARKDOWN_TEXTFIELD).performTextInput("a")
        composeTestRule.assertAllAutoCompletionHintsHidden()
    }

    @Test
    fun `test GIVEN the autoCompletion dialog WHEN removing the tag character @ THEN the autoCompletion dialog is hidden`() {
        composeTestRule.onNodeWithTag(TEST_TAG_MARKDOWN_TEXTFIELD).performTextInput("@")
        composeTestRule.assertAllAutoCompletionHintsShown()

        composeTestRule.onNodeWithTag(TEST_TAG_MARKDOWN_TEXTFIELD).performTextClearance()
        composeTestRule.assertAllAutoCompletionHintsHidden()
    }

    @Test
    fun `test GIVEN the autoCompletion has been performed WHEN entering the tag character again THEN the autoCompletion dialog shows again`() {
        composeTestRule.onNodeWithTag(TEST_TAG_MARKDOWN_TEXTFIELD).performTextInput("@")
        composeTestRule.onNodeWithText("User1").performClick()
        composeTestRule.onNodeWithTag(TEST_TAG_MARKDOWN_TEXTFIELD).assertTextEquals("<User1>")
        composeTestRule.assertAllAutoCompletionHintsHidden()

        composeTestRule.onNodeWithTag(TEST_TAG_MARKDOWN_TEXTFIELD).performTextInput("@")
        composeTestRule.assertAllAutoCompletionHintsShown()
    }

    @Test
    fun `test GIVEN the textField WHEN entering a first and surname separated by a single whitespace THEN the dialog shows`() {
        composeTestRule.onNodeWithTag(TEST_TAG_MARKDOWN_TEXTFIELD).performTextInput("@FirstName SurName")
        composeTestRule.assertAllAutoCompletionHintsShown()
    }

    @Test
    fun `test GIVEN the textField WHEN entering a second whitespace THEN the dialog is hidden`() {
        composeTestRule.onNodeWithTag(TEST_TAG_MARKDOWN_TEXTFIELD).performTextInput("@FirstName SurName ")
        composeTestRule.assertAllAutoCompletionHintsHidden()
    }



    private fun ComposeContentTestRule.assertAllAutoCompletionHintsHidden() {
        onNodeWithText("User1").assertDoesNotExist()
        onNodeWithText("User2").assertDoesNotExist()
        onNodeWithText("User3").assertDoesNotExist()
    }

    private fun ComposeContentTestRule.assertAllAutoCompletionHintsShown() {
        onNodeWithText("User1").assertExists()
        onNodeWithTag(TEST_TAG_REPLY_AUTO_COMPLETE_POPUP_LIST).performScrollToIndex(1)
        onNodeWithText("User2").assertExists()
        onNodeWithTag(TEST_TAG_REPLY_AUTO_COMPLETE_POPUP_LIST).performScrollToIndex(2)
        onNodeWithText("User3").assertExists()
    }
}
