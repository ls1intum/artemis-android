package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.BaseThreadUITest
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.TestInitialReplyTextProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.impl.EmojiServiceStub
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.ChatListItem
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.MetisChatList
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.PostsDataState
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.PostPojo
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CompletableDeferred
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class ReplyTextFieldVisibilityUITest : BaseThreadUITest() {

    @Test
    fun `test GIVEN the thread view is shown containing one post and three answer posts WHEN the markdown text field is clicked THEN the keyboard is shown below the markdown text field`() {
        setupThreadUi(posts[0]) { CompletableDeferred() }
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

    private fun setupChatUi(posts: List<PostPojo>) {
        composeTestRule.setContent {
            val list = posts.map { post -> ChatListItem.PostChatListItem(post) }.toMutableList()
            MetisChatList(
                modifier = Modifier.fillMaxSize(),
                initialReplyTextProvider = remember { TestInitialReplyTextProvider() },
                posts = PostsDataState.Loaded.WithList(list, PostsDataState.NotLoading),
                clientId = clientId,
                hasModerationRights = false,
                isAtLeastTutorInCourse = false,
                isConversationCreator = false,
                listContentPadding = PaddingValues(),
                serverUrl = "",
                courseId = course.id!!,
                state = rememberLazyListState(),
                emojiService = EmojiServiceStub,
                bottomItem = posts.lastOrNull(),
                isReplyEnabled = true,
                onCreatePost = { CompletableDeferred() },
                onEditPost = { _, _ -> CompletableDeferred() },
                onPinPost = { CompletableDeferred() },
                onDeletePost = { CompletableDeferred() },
                onRequestReactWithEmoji = { _, _, _ -> CompletableDeferred() },
                onClickViewPost = {},
                onRequestRetrySend = { _ -> },
                title = "Title"
            )
        }
    }
}
