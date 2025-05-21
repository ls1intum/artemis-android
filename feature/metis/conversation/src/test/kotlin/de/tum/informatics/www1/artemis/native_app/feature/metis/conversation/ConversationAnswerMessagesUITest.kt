package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasAnyChild
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performSemanticsAction
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.thread.TEST_TAG_THREAD_LIST
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.AnswerPostPojo
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.PostPojo
import kotlinx.coroutines.CompletableDeferred
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class ConversationAnswerMessagesUITest : BaseChatUITest() {

    private fun testTagForAnswerPost(answerPostId: String) = "answerPost$answerPostId"

    private val post = posts[0]

    @Test
    fun `test GIVEN post is not resolved WHEN resolving the post THEN the post is resolved with the first answer post`() {
        markAnswerAsResolvingImpl(0)
    }

    @Test
    fun `test GIVEN post is not resolved WHEN resolving the post THEN the post is resolved with the third answer post`() {
        markAnswerAsResolvingImpl(2)
    }

    @Test
    fun `test GIVEN post is resolved WHEN un-resolving the post THEN the post is un-resolved`() {
        val resolvingIndex = 0
        val resolvedPost = createResolvedPost(resolvingIndex)

        var unresolvedPost: IBasePost? = null

        setupThreadUi(
            post = resolvedPost,
            onResolvePost = { post ->
                unresolvedPost = post
                CompletableDeferred()
            },
        )

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
        setupThreadUi(post)

        composeTestRule.onNodeWithText(post.content).assertExists()
        composeTestRule.onNodeWithText(context.getString(R.string.post_is_resolved))
            .assertDoesNotExist()

        for ((index, answer) in answers.withIndex()) {
            scrollToAnswerPostIndex(index)
            composeTestRule.onNodeWithText(answer.content!!).assertExists()
            composeTestRule.onNodeWithText(context.getString(R.string.post_resolves))
                .assertDoesNotExist()
        }
    }

    @Test
    fun `test GIVEN the post is resolved and one answer post is marked as resolving THEN the post is shown as resolved and this answer post is shown as resolving`() {
        val resolvingIndex = 2

        val resolvedPost = createResolvedPost(resolvingIndex)

        setupThreadUi(resolvedPost)

        val resolvesAssertion = hasAnyChild(hasText(context.getString(R.string.post_resolves)))

        for (i in answers.indices) {
            scrollToAnswerPostIndex(i)

            composeTestRule
                .onNodeWithTag(testTagForAnswerPost(answers[i].postId))
                .assert(if (i == resolvingIndex) resolvesAssertion else resolvesAssertion.not())
        }
    }

    private fun createResolvedPost(resolvingIndex: Int): PostPojo {
        val modifiedAnswers = answers.toMutableList()
        modifiedAnswers[resolvingIndex] = modifiedAnswers[resolvingIndex].copy(resolvesPost = true)
        val resolvedPost = post.copy(
            resolved = true,
            answers = modifiedAnswers
        )
        return resolvedPost
    }


    private fun markAnswerAsResolvingImpl(index: Int) {
        var resolvedPost: IBasePost? = null

        setupThreadUi(
            post = post,
            onResolvePost = { post ->
                resolvedPost = post
                CompletableDeferred()
            }
        )

        scrollToAnswerPostIndex(index)
        composeTestRule.onNodeWithText(answers[index].content!!, useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.OnLongClick)
        composeTestRule.onNodeWithText(context.getString(R.string.post_resolves)).performClick()

        assert(resolvedPost != null)
        assert(resolvedPost is AnswerPostPojo)
        assert((resolvedPost as AnswerPostPojo).content == answers[index].content!!)
    }

    private fun scrollToAnswerPostIndex(index: Int) {
        composeTestRule
            .onNodeWithTag(TEST_TAG_THREAD_LIST, useUnmergedTree = true)
            .performScrollToIndex(index)
    }
}