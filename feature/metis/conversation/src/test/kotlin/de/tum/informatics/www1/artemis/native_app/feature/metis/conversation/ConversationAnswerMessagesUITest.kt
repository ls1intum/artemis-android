package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.thread.MetisThreadUi
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.UserRole
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.AnswerPostPojo
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.PostPojo
import kotlinx.coroutines.CompletableDeferred
import kotlinx.datetime.Clock
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.robolectric.RobolectricTestRunner
import kotlin.random.Random
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class ConversationAnswerMessagesUITest : KoinTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context: Context get() = InstrumentationRegistry.getInstrumentation().context

    private val clientId = 20L

    private val course: Course = Course(id = 1)
    private val conversation = OneToOneChat(id = 2)

    private val post = PostPojo(
        clientPostId = "client-id",
        serverPostId = 12,
        content = "Post content",
        resolved = false,
        updatedDate = null,
        creationDate = Clock.System.now(),
        authorId = clientId,
        title = null,
        authorName = "author name",
        authorRole = UserRole.USER,
        courseWideContext = null,
        tags = emptyList(),
        answers = listOf(
            AnswerPostPojo(
                parentPostId = "client-id",
                postId = "answer-client-id",
                resolvesPost = false,
                basePostingCache = AnswerPostPojo.BasePostingCache(
                    serverPostId = 13,
                    authorId = clientId,
                    creationDate = Clock.System.now(),
                    updatedDate = null,
                    content = "Answer Post content 0",
                    authorRole = UserRole.USER,
                    authorName = "author name"
                ),
                reactions = emptyList(),
                serverPostIdCache = AnswerPostPojo.ServerPostIdCache(
                    serverPostId = 13
                )
            ),
            AnswerPostPojo(
                parentPostId = "client-id",
                postId = "answer-client-id-1",
                resolvesPost = false,
                basePostingCache = AnswerPostPojo.BasePostingCache(
                    serverPostId = 14,
                    authorId = clientId,
                    creationDate = Clock.System.now(),
                    updatedDate = null,
                    content = "Answer Post content 1",
                    authorRole = UserRole.USER,
                    authorName = "author name"
                ),
                reactions = emptyList(),
                serverPostIdCache = AnswerPostPojo.ServerPostIdCache(
                    serverPostId = 14
                )
            ),
            AnswerPostPojo(
                parentPostId = "client-id",
                postId = "answer-client-id-2",
                resolvesPost = false,
                basePostingCache = AnswerPostPojo.BasePostingCache(
                    serverPostId = 15,
                    authorId = clientId,
                    creationDate = Clock.System.now(),
                    updatedDate = null,
                    content = "Answer Post content 2",
                    authorRole = UserRole.USER,
                    authorName = "author name"
                ),
                reactions = emptyList(),
                serverPostIdCache = AnswerPostPojo.ServerPostIdCache(
                    serverPostId = 15
                )
            )
        ),
        reactions = emptyList()
    )

    @Test
    fun `test GIVEN post is not resolved WHEN resolving the post THEN the post is resolved with the first answer post`() {
        var resolvedPost: IBasePost? = null

        composeTestRule.setContent {
            MetisThreadUi(
                modifier = Modifier.fillMaxSize(),
                courseId = course.id!!,
                clientId = clientId,
                postDataState = DataState.Success(post),
                conversationDataState = DataState.Success(conversation),
                hasModerationRights = false,
                isAtLeastTutorInCourse = false,
                serverUrl = "",
                initialReplyTextProvider = remember { TestInitialReplyTextProvider() },
                onCreatePost = { CompletableDeferred() },
                onEditPost = { _, _ -> CompletableDeferred() },
                onResolvePost = { post ->
                    resolvedPost = post
                    CompletableDeferred()
                },
                onDeletePost = { CompletableDeferred() },
                onRequestReactWithEmoji = { _, _, _ -> CompletableDeferred() },
                onRequestReload = {},
                onRequestRetrySend = { _, _ -> },
            )
        }

        composeTestRule.onNodeWithText("Answer Post content 0").performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.post_resolves)).performClick()

        assert(resolvedPost != null)
        assert(resolvedPost is AnswerPostPojo)
        assert((resolvedPost as AnswerPostPojo).content == "Answer Post content 0")
    }

    @Test
    fun `test GIVEN post is not resolved WHEN resolving the post THEN the post is resolved with the third answer post`() {
        var resolvedPost: IBasePost? = null

        composeTestRule.setContent {
            MetisThreadUi(
                modifier = Modifier.fillMaxSize(),
                courseId = course.id!!,
                clientId = clientId,
                postDataState = DataState.Success(post),
                conversationDataState = DataState.Success(conversation),
                hasModerationRights = false,
                isAtLeastTutorInCourse = false,
                serverUrl = "",
                initialReplyTextProvider = remember { TestInitialReplyTextProvider() },
                onCreatePost = { CompletableDeferred() },
                onEditPost = { _, _ -> CompletableDeferred() },
                onResolvePost = { post ->
                    resolvedPost = post
                    CompletableDeferred()
                },
                onDeletePost = { CompletableDeferred() },
                onRequestReactWithEmoji = { _, _, _ -> CompletableDeferred() },
                onRequestReload = {},
                onRequestRetrySend = { _, _ -> },
            )
        }

        composeTestRule.onNodeWithText("Answer Post content 2").performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.post_resolves)).performClick()

        assert(resolvedPost != null)
        assert(resolvedPost is AnswerPostPojo)
        assert((resolvedPost as AnswerPostPojo).content == "Answer Post content 2")
    }

    @Test
    fun `test GIVEN post is resolved WHEN un-resolving the post THEN the post is un-resolved`() {
        var resolvingIndex = 0
        val resolvedPost = post.copy(
            resolved = true,
            answers = post.answers.mapIndexed { index, answer ->
                resolvingIndex = post.answers.indexOfFirst { !it.resolvesPost }
                if (!answer.resolvesPost && index == resolvingIndex) {
                    // Update only the first unresolved answer
                    answer.copy(resolvesPost = true)
                } else {
                    answer
                }
            }
        )

        var unresolvedPost: IBasePost? = null

        composeTestRule.setContent {
            MetisThreadUi(
                modifier = Modifier.fillMaxSize(),
                courseId = course.id!!,
                clientId = clientId,
                postDataState = DataState.Success(resolvedPost),
                conversationDataState = DataState.Success(conversation),
                hasModerationRights = false,
                isAtLeastTutorInCourse = false,
                serverUrl = "",
                initialReplyTextProvider = remember { TestInitialReplyTextProvider() },
                onCreatePost = { CompletableDeferred() },
                onEditPost = { _, _ -> CompletableDeferred() },
                onResolvePost = { post ->
                    unresolvedPost = post
                    CompletableDeferred()
                },
                onDeletePost = { CompletableDeferred() },
                onRequestReactWithEmoji = { _, _, _ -> CompletableDeferred() },
                onRequestReload = {},
                onRequestRetrySend = { _, _ -> },
            )
        }

        composeTestRule.onNodeWithText("Answer Post content $resolvingIndex")
        composeTestRule.onNodeWithText(context.getString(R.string.post_does_not_resolve))
            .performClick()

        assert(unresolvedPost != null)
        assert(unresolvedPost is AnswerPostPojo)
        assert((unresolvedPost as AnswerPostPojo).content == "Answer Post content $resolvingIndex")
    }

    @Test
    fun `test GIVEN the post is not resolved and no answer post is resolving THEN the post is shown as not resolved and no answer post is shown as resolving`() {
        composeTestRule.setContent {
            MetisThreadUi(
                modifier = Modifier.fillMaxSize(),
                courseId = course.id!!,
                clientId = clientId,
                postDataState = DataState.Success(post),
                conversationDataState = DataState.Success(conversation),
                hasModerationRights = false,
                isAtLeastTutorInCourse = false,
                serverUrl = "",
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

        composeTestRule.onNodeWithText("Post content").assertExists()
        composeTestRule.onNodeWithText("Answer Post content 0").assertExists()
        composeTestRule.onNodeWithText("Answer Post content 1").assertExists()
        composeTestRule.onNodeWithText("Answer Post content 2").assertExists()
        composeTestRule.onNodeWithText(context.getString(R.string.post_is_resolved))
            .assertDoesNotExist()
        composeTestRule.onNodeWithText(context.getString(R.string.post_resolves))
            .assertDoesNotExist()
    }

    @Test
    fun `test GIVEN the post is resolved and one answer post is marked as resolving THEN the post is shown as resolved and this answer post is shown as resolving`() {
        var resolvingIndex = 0
        val resolvedPost = post.copy(
            resolved = true,
            answers = post.answers.mapIndexed { index, answer ->
                resolvingIndex = post.answers.indexOfFirst { !it.resolvesPost }
                if (!answer.resolvesPost && index == resolvingIndex) {
                    // Update only the first unresolved answer
                    answer.copy(resolvesPost = true)
                } else {
                    answer
                }
            }
        )

        composeTestRule.setContent {
            MetisThreadUi(
                modifier = Modifier.fillMaxSize(),
                courseId = course.id!!,
                clientId = clientId,
                postDataState = DataState.Success(resolvedPost),
                conversationDataState = DataState.Success(conversation),
                hasModerationRights = false,
                isAtLeastTutorInCourse = false,
                serverUrl = "",
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

        composeTestRule.onNodeWithText("Post content").assertExists()
        composeTestRule.onNodeWithText(context.getString(R.string.post_is_resolved)).assertExists()
        composeTestRule.onNodeWithText(context.getString(R.string.post_resolves)).assertExists()
    }
}