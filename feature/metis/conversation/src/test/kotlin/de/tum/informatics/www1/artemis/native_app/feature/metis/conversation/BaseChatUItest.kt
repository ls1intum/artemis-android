package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.impl.EmojiServiceStub
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.ChatListItem
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.MetisChatList
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.PostsDataState
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.thread.MetisThreadUi
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IStandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.UserRole
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.AnswerPostPojo
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.PostPojo
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.datetime.Clock

abstract class BaseChatUItest : BaseComposeTest() {

    val clientId = 20L

    private val course: Course = Course(id = 1)
    val conversation = OneToOneChat(id = 2)

    val answers = (0..2).map { index ->
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

    val posts = (0..2).map { index ->
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

    fun setupThreadUi(
        post: PostPojo,
        onResolvePost: ((IBasePost) -> Deferred<MetisModificationFailure>)?
    ) {
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
                onResolvePost = onResolvePost,
                onDeletePost = { CompletableDeferred() },
                onRequestReactWithEmoji = { _, _, _ -> CompletableDeferred() },
                onRequestReload = {},
                onRequestRetrySend = { _, _ -> },
            )
        }
    }

    fun setupChatUi(
        posts: List<IStandalonePost>,
        currentUser: User = User(id = clientId),
        hasModerationRights: Boolean = false
    ) {
        composeTestRule.setContent {
            val list = posts.map { post -> ChatListItem.PostChatListItem(post) }.toMutableList()
            MetisChatList(
                modifier = Modifier.fillMaxSize(),
                initialReplyTextProvider = remember { TestInitialReplyTextProvider() },
                posts = PostsDataState.Loaded.WithList(list, PostsDataState.NotLoading),
                clientId = currentUser.id,
                hasModerationRights = hasModerationRights,
                isAtLeastTutorInCourse = false,
                listContentPadding = PaddingValues(),
                serverUrl = "",
                courseId = course.id!!,
                state = rememberLazyListState(),
                emojiService = EmojiServiceStub,
                bottomItem = null,
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
