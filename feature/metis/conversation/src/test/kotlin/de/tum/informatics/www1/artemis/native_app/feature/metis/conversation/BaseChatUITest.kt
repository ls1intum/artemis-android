package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.LocalArtemisImageProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.test.ArtemisImageProviderStub
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.impl.EmojiServiceStub
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.ChatListItem
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.MetisChatList
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.PostsDataState
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions.PostActionFlags
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.thread.ConversationThreadUseCase
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.thread.MetisThreadUi
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.AnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.DisplayPriority
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IStandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.UserRole
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ConversationUser
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.AnswerPostPojo
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.PostPojo
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter.LocalVisibleMetisContextManager
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter.VisiblePostList
import de.tum.informatics.www1.artemis.native_app.feature.metistest.VisibleMetisContextManagerMock
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Clock


private fun User.asConversationUser(isRequestingUser: Boolean = false): ConversationUser {
    return ConversationUser(
        id = id,
        name = name,
        imageUrl = null,
        isRequestingUser = isRequestingUser
    )
}

abstract class BaseChatUITest : BaseComposeTest() {

    val clientId = 20L

    private val course: Course = Course(id = 1)

    val currentUser = User(
        id = clientId,
        name = "Current user"
    )
    val otherUser = User(
        id = 1234,
        name = "Other user"
    )

    val conversation = OneToOneChat(
        id = 2,
        members = listOf(
            currentUser.asConversationUser(isRequestingUser = true),
            otherUser.asConversationUser()
        )
    )

    val reactions = listOf(
        PostPojo.Reaction(
            emojiId = "rocket",
            authorId = currentUser.id,
            username = currentUser.username ?: "author name",
            id = 1,
            creationDate = Clock.System.now()
        ),
        PostPojo.Reaction(
            emojiId = "tada",
            authorId = otherUser.id,
            username = otherUser.username ?: "other author name",
            id = 2,
            creationDate = Clock.System.now()
        )
    )

    val simplePostContent = "Simple post content"
    val simpleAnswerContent = "Simple answer content"

    fun simplePost(
        postAuthor: User,
        isSaved: Boolean = false
    ): StandalonePost = StandalonePost(
        id = 1,
        author = postAuthor,
        content = simplePostContent,
        isSaved = isSaved
    )

    fun simpleThreadPostWithAnswer(
        postAuthor: User,
        answerAuthor: User,
    ): StandalonePost {
        val basePost = simplePost(postAuthor)
        val answerPost = AnswerPost(
            id = 2,
            author = answerAuthor,
            content = simpleAnswerContent,
            post = basePost
        )
        return basePost.copy(answers = listOf(answerPost))
    }

    val answers = (0..2).map { index ->
        AnswerPostPojo(
            parentPostId = "client-id",
            parentAuthorIdCache = AnswerPostPojo.ParentAuthorIdCache(clientId),
            postId = "answer-client-id-$index",
            resolvesPost = false,
            basePostingCache = AnswerPostPojo.BasePostingCache(
                serverPostId = index.toLong(),
                authorId = clientId,
                creationDate = Clock.System.now(),
                updatedDate = null,
                content = "Answer Post content $index",
                authorRole = UserRole.USER,
                authorName = "author name",
                authorImageUrl = null,
                isSaved = false,
                hasForwardedMessages = false
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
            authorImageUrl = null,
            courseWideContext = null,
            tags = emptyList(),
            answers = if (index == 0) answers else emptyList(),
            reactions = if (index == 0) reactions else emptyList(),
            displayPriority = DisplayPriority.NONE,
            isSaved = false,
            hasForwardedMessages = index == 1
        )
    }

    private val forwardedPosts = listOf(
        posts[2].copy(
            clientPostId = "client-id-forwarded",
            serverPostId = 10101.toLong(),
            content = "Post content forwarded",
            authorName = "author name forwarded"
        )
    )

    fun setupThreadUi(
        post: IStandalonePost,
        onResolvePost: ((IBasePost) -> Deferred<MetisModificationFailure>)? = { CompletableDeferred() },
        onPinPost: ((IBasePost) -> Deferred<MetisModificationFailure>)? = { CompletableDeferred() },
        isAbleToPin: Boolean = false,
        isAtLeastTutorInCourse: Boolean = false,
        hasModerationRights: Boolean = false,
    ) {
        val threadUseCase = mockk<ConversationThreadUseCase>()
        val testFlow = MutableStateFlow<ChatListItem.PostItem.ThreadItem.Answer?>(null)
        every { threadUseCase.getAnswerChatListItem(any()) } returns testFlow

        val chatListItem = if (post.hasForwardedMessages == true) {
            ChatListItem.PostItem.ThreadItem.ContextItem.ContextPostWithForwardedMessage(post, forwardedPosts, course.id!!)
        } else {
            ChatListItem.PostItem.ThreadItem.ContextItem.ContextPost(post)
        }

        composeTestRule.setContent {
            MetisThreadUi(
                modifier = Modifier.fillMaxSize(),
                courseId = course.id!!,
                clientId = clientId,
                postDataState = DataState.Success(post),
                conversationDataState = DataState.Success(conversation),
                postActionFlags = PostActionFlags(
                    isAbleToPin = isAbleToPin,
                    isAtLeastTutorInCourse = isAtLeastTutorInCourse,
                    hasModerationRights = hasModerationRights,
                ),
                serverUrl = "",
                isMarkedAsDeleteList = mutableStateListOf(),
                emojiService = EmojiServiceStub,
                chatListContextItem = chatListItem,
                answerChatListItemState = { answer -> threadUseCase.getAnswerChatListItem(answer) },
                initialReplyTextProvider = remember { TestInitialReplyTextProvider() },
                onCreatePost = { CompletableDeferred() },
                onEditPost = { _, _ -> CompletableDeferred() },
                onResolvePost = onResolvePost,
                onPinPost = onPinPost,
                onSavePost = { CompletableDeferred() },
                onDeletePost = { CompletableDeferred() },
                onUndoDeletePost = {},
                onRequestReactWithEmoji = { _, _, _ -> CompletableDeferred() },
                onRequestReload = {},
                onRequestRetrySend = { _, _ -> },
                onFileSelect = { _, _ -> },
            )
        }
    }

    @SuppressLint("UnrememberedMutableState")
    fun setupChatUi(
        posts: List<IStandalonePost>,
        currentUser: User = User(id = clientId),
        isAbleToPin: Boolean = false,
        isAtLeastTutorInCourse: Boolean = false,
        hasModerationRights: Boolean = false,
        onPinPost: (IStandalonePost) -> Deferred<MetisModificationFailure> = { CompletableDeferred() }
    ) {
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalArtemisImageProvider provides ArtemisImageProviderStub(),
                LocalVisibleMetisContextManager provides VisibleMetisContextManagerMock.also {
                    it.registerMetisContext(VisiblePostList(MetisContext.Conversation(
                        courseId = course.id!!,
                        conversationId = conversation.id
                    )))
                }
            ) {
                val list = posts.map { post ->
                    if (post.hasForwardedMessages == true) {
                        ChatListItem.PostItem.IndexedItem.PostWithForwardedMessage(
                            post = post,
                            answers = post.answers.orEmpty(),
                            forwardedPosts = forwardedPosts,
                            courseId = course.id!!
                        )
                    } else {
                        ChatListItem.PostItem.IndexedItem.Post(post, post.answers.orEmpty())
                    }
                }.toMutableList()
                MetisChatList(
                    modifier = Modifier.fillMaxSize(),
                    initialReplyTextProvider = remember { TestInitialReplyTextProvider() },
                    posts = PostsDataState.Loaded.WithList(list, PostsDataState.NotLoading),
                    clientId = currentUser.id,
                    postActionFlags = PostActionFlags(
                        isAbleToPin = isAbleToPin,
                        isAtLeastTutorInCourse = isAtLeastTutorInCourse,
                        hasModerationRights = hasModerationRights,),
                    serverUrl = "",
                    courseId = course.id!!,
                    state = rememberLazyListState(),
                    emojiService = EmojiServiceStub,isMarkedAsDeleteList = mutableStateListOf(),
                    bottomItem = null,
                    isReplyEnabled = true,
                    onCreatePost = { CompletableDeferred() },
                    onEditPost = { _, _ -> CompletableDeferred() },
                    onPinPost = onPinPost,
                    onSavePost = { CompletableDeferred() },
                    onDeletePost = { CompletableDeferred() },
                    onUndoDeletePost = {},
                    onRequestReactWithEmoji = { _, _, _ -> CompletableDeferred() },
                    onClickViewPost = {},
                    onRequestRetrySend = { _ -> },
                    conversationName = "Title",
                    onFileSelected = { _ -> }
                )
            }
        }
    }
}