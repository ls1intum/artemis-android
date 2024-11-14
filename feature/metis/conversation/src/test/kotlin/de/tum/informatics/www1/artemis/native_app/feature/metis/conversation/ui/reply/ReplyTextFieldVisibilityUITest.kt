package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.paging.LoadState
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.TestInitialReplyTextProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.impl.EmojiServiceStub
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.ChatListItem
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.MetisChatList
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.PostsDataState
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.asPostsDataState
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.thread.MetisThreadUi
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.UserRole
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.AnswerPostPojo
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.PostPojo
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.datetime.Clock
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
class ReplyTextFieldVisibilityUITest : BaseComposeTest() {

    private val clientId = 20L

    private val course: Course = Course(id = 1)
    private val conversation = OneToOneChat(id = 2)

    private val answers = (0..2).map { index ->
        AnswerPostPojo(
            parentPostId = "client-id",
            postId = "answer-client-id-$index",
            resolvesPost = false,
            basePostingCache = AnswerPostPojo.BasePostingCache(
                serverPostId = index.toLong(),
                authorId = clientId,
                creationDate = Clock.System.now(),
                updatedDate = null,
                content = "Answer Post content $index",
                authorRole = UserRole.USER,
                authorName = "author name"
            ),
            reactions = emptyList(),
            serverPostIdCache = AnswerPostPojo.ServerPostIdCache(
                serverPostId = index.toLong()
            )
        )
    }

    private val posts = (0..2).map { index ->
        PostPojo(
            clientPostId = "client-id-$index",
            serverPostId = index.toLong(),
            content = "Post content $index",
            resolved = false,
            updatedDate = null,
            creationDate = Clock.System.now(),
            authorId = clientId,
            title = null,
            authorName = "author name",
            authorRole = UserRole.USER,
            courseWideContext = null,
            tags = emptyList(),
            answers = if (index == 0) answers else emptyList(),
            reactions = emptyList()
        )
    }

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

    private fun setupThreadUi(post: PostPojo) {
        composeTestRule.setContent {
            MetisThreadUi(
                modifier = Modifier.fillMaxSize(),
                courseId = course.id!!,
                clientId = clientId,
                postDataState = DataState.Success(post),
                conversationDataState = DataState.Success(conversation),
                hasModerationRights = false,
                isAtLeastTutorInCourse = false,
                listContentPadding = PaddingValues(),
                serverUrl = "",
                emojiService = EmojiServiceStub,
                initialReplyTextProvider = remember { TestInitialReplyTextProvider() },
                onCreatePost = { CompletableDeferred() },
                onEditPost = { _, _ -> CompletableDeferred() },
                onResolvePost = { CompletableDeferred() },
                onDeletePost = { CompletableDeferred() },
                onRequestReactWithEmoji = { _, _, _ -> CompletableDeferred() },
                onRequestReload = {},
                onRequestRetrySend = { _, _ -> },
            )
        }
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
                listContentPadding = PaddingValues(),
                serverUrl = "",
                courseId = course.id!!,
                state = rememberLazyListState(),
                emojiService = EmojiServiceStub,
                bottomItem = posts.lastOrNull(),
                isReplyEnabled = true,
                onCreatePost = { CompletableDeferred() },
                onEditPost = { _, _ -> CompletableDeferred() },
                onDeletePost = { CompletableDeferred() },
                onRequestReactWithEmoji = { _, _, _ -> CompletableDeferred() },
                onClickViewPost = {},
                onRequestRetrySend = { _ -> },
                title = "Title"
            )
        }
    }
}
