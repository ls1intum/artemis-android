package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.hasAnyChild
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.printToLog
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.impl.EmojiServiceStub
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.thread.MetisThreadUi
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.UserRole
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.AnswerPostPojo
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.PostPojo
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ConversationAnswerMessagesUITest : BaseComposeTest() {

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
        answers = answers,
        reactions = emptyList()
    )

    private fun testTagForAnswerPost(answerPostId: String) = "answerPost$answerPostId"

    @Test
    fun `test GIVEN post is not resolved WHEN resolving the post THEN the post is resolved with the first answer post`() {
        var resolvedPost: IBasePost? = null

        setupUi(post) { post ->
            resolvedPost = post
            CompletableDeferred()
        }

        composeTestRule.onNodeWithText(answers[0].content!!, useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.OnLongClick)
        composeTestRule.onNodeWithText(context.getString(R.string.post_resolves))
            .performClick()

        assert(resolvedPost != null)
        assert(resolvedPost is AnswerPostPojo)
        assert((resolvedPost as AnswerPostPojo).content == answers[0].content!!)
    }

    @Test
    fun `test GIVEN post is not resolved WHEN resolving the post THEN the post is resolved with the third answer post`() {
        var resolvedPost: IBasePost? = null

        setupUi(post) { post ->
            resolvedPost = post
            CompletableDeferred()
        }

        composeTestRule.onNodeWithText(answers[2].content!!, useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.OnLongClick)
        composeTestRule.onNodeWithText(context.getString(R.string.post_resolves)).performClick()

        assert(resolvedPost != null)
        assert(resolvedPost is AnswerPostPojo)
        assert((resolvedPost as AnswerPostPojo).content == answers[2].content!!)
    }

    @Test
    fun `test GIVEN post is resolved WHEN un-resolving the post THEN the post is un-resolved`() {
        val resolvingIndex = 0

        val modifiedAnswers = answers.toMutableList()
        modifiedAnswers[resolvingIndex] = modifiedAnswers[resolvingIndex].copy(resolvesPost = true)
        val resolvedPost = post.copy(
            resolved = true,
            answers = modifiedAnswers
        )

        var unresolvedPost: IBasePost? = null

        setupUi(resolvedPost) { post ->
            unresolvedPost = post
            CompletableDeferred()
        }

        composeTestRule.onNodeWithText(answers[resolvingIndex].content!!, useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.OnLongClick)
        composeTestRule.onNodeWithText(context.getString(R.string.post_does_not_resolve))
            .performClick()

        assert(unresolvedPost != null)
        assert(unresolvedPost is AnswerPostPojo)
        assert((unresolvedPost as AnswerPostPojo).content == answers[resolvingIndex].content!!)
    }

    @Test
    fun `test GIVEN the post is not resolved and no answer post is resolving THEN the post is shown as not resolved and no answer post is shown as resolving`() {
        setupUi(post) { CompletableDeferred() }

        composeTestRule.onNodeWithText(post.content).assertExists()
        for (answer in answers) {
            composeTestRule.onNodeWithText(answer.content!!).assertExists()
        }
        composeTestRule.onNodeWithText(context.getString(R.string.post_is_resolved))
            .assertDoesNotExist()
        composeTestRule.onNodeWithText(context.getString(R.string.post_resolves))
            .assertDoesNotExist()
    }

    @Test
    fun `test GIVEN the post is resolved and one answer post is marked as resolving THEN the post is shown as resolved and this answer post is shown as resolving`() {
        val resolvingIndex = 0

        val modifiedAnswers = answers.toMutableList()
        modifiedAnswers[resolvingIndex] = modifiedAnswers[resolvingIndex].copy(resolvesPost = true)
        val resolvedPost = post.copy(
            resolved = true,
            answers = modifiedAnswers
        )

        val listState = LazyListState()
        setupUi(resolvedPost, listState) { CompletableDeferred() }

        composeTestRule
            .onNodeWithTag(testTagForAnswerPost(answers[resolvingIndex].postId), useUnmergedTree = true)
            .assert(hasAnyChild(hasText(context.getString(R.string.post_resolves))))
        composeTestRule
            .onNodeWithTag(testTagForAnswerPost(answers[1].postId), useUnmergedTree = true)
            .assert(hasAnyChild(hasText(context.getString(R.string.post_resolves)).not()))

        runBlocking {
            listState.scrollToItem(index = 2)
        }

        composeTestRule
            .onNodeWithTag(testTagForAnswerPost(answers[2].postId), useUnmergedTree = true)
            .assert(hasAnyChild(hasText(context.getString(R.string.post_resolves)).not()))
    }

    private fun setupUi(
        post: PostPojo,
        listState: LazyListState = LazyListState(),
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
                serverUrl = "",
                emojiService = EmojiServiceStub,
                listState = listState,
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
}