package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.StandalonePostId
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.DisplayPriority
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.PostPojo
import kotlinx.coroutines.CompletableDeferred
import org.junit.Ignore
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
@Ignore("There is an open issue about onClick events not working for the ModalBottomSheetLayout with" +
        "the robolectric test runner. Enable this test again as soon as the following issue is resolved:" +
        "https://github.com/robolectric/robolectric/issues/9595")
class ConversationMessagesUITest : BaseChatUITest() {

    private fun testTagForPost(postId: StandalonePostId?) = "post$postId"

    @Test
    fun `test GIVEN post is not pinned WHEN pinning the post THEN the correct post gets pinned`() {
        var changedPost: IBasePost? = null

        setupChatUi(posts = posts, onPinPost =  { post ->
            changedPost = post
            CompletableDeferred()
        })

        composeTestRule.onNodeWithTag(
            testTagForPost(posts[0].standalonePostId),
            useUnmergedTree = true
        )
            .performSemanticsAction(SemanticsActions.OnLongClick)
        composeTestRule.onNodeWithText(context.getString(R.string.post_pin))
            .performClick()

        testPinDestination(changedPost)
    }

    @Test
    fun `test GIVEN post is pinned WHEN unpinning the post THEN the correct post gets unpinned`() {
        var changedPost: IBasePost? = null
        val modifiedPosts = posts.toMutableList()
        modifiedPosts[0] = modifiedPosts[0].copy(displayPriority = DisplayPriority.PINNED)

        setupChatUi(posts = modifiedPosts, onPinPost =  { post ->
            changedPost = post
            CompletableDeferred()
        })

        composeTestRule.onNodeWithTag(
            testTagForPost(posts[0].standalonePostId),
            useUnmergedTree = true
        )
            .performSemanticsAction(SemanticsActions.OnLongClick)
        composeTestRule.onNodeWithText(context.getString(R.string.post_unpin))
            .performClick()

        testPinDestination(changedPost)
    }

    @Test
    fun `test GIVEN post is not pinned in thread WHEN pinning the post THEN the correct post gets pinned in thread`() {
        var changedPost: IBasePost? = null

        setupThreadUi(
            post = posts[0],
            onPinPost = { post ->
                changedPost = post
                CompletableDeferred()
            }
        )

        composeTestRule.onNodeWithTag(
            testTagForPost(posts[0].standalonePostId),
            useUnmergedTree = true
        )
            .performSemanticsAction(SemanticsActions.OnLongClick)
        composeTestRule.onNodeWithText(context.getString(R.string.post_pin))
            .performClick()

        testPinDestination(changedPost)
    }

    @Test
    fun `test GIVEN post is pinned in thread WHEN unpinning the post THEN the correct post gets unpinned in thread`() {
        var changedPost: IBasePost? = null
        val modifiedPosts = posts.toMutableList()
        modifiedPosts[0] = modifiedPosts[0].copy(displayPriority = DisplayPriority.PINNED)

        setupThreadUi(
            post = modifiedPosts[0],
            onPinPost = { post ->
                changedPost = post
                CompletableDeferred()
            })

        composeTestRule.onNodeWithTag(
            testTagForPost(posts[0].standalonePostId),
            useUnmergedTree = true
        )
            .performSemanticsAction(SemanticsActions.OnLongClick)
        composeTestRule.onNodeWithText(context.getString(R.string.post_unpin))
            .performClick()

        testPinDestination(changedPost)
    }

    @Test
    fun `test GIVEN the post is not pinned THEN the post is not shown as pinned`() {
        setupChatUi(posts)

        testPinnedLabelInvisibility()
    }

    @Test
    fun `test GIVEN the post is not pinned in thread THEN the post is not shown as pinned in thread`() {
        setupThreadUi(posts[0])

        testPinnedLabelInvisibility()
    }

    @Test
    fun `test GIVEN the post is pinned THEN the post is shown as pinned`() {
        val modifiedPosts = posts.toMutableList()
        modifiedPosts[0] = modifiedPosts[0].copy(displayPriority = DisplayPriority.PINNED)
        setupChatUi(modifiedPosts)

        testPinnedLabelVisibility()
    }

    @Test
    fun `test GIVEN the post is pinned in thread THEN the post is shown as pinned in thread`() {
        setupThreadUi(posts[0].copy(displayPriority = DisplayPriority.PINNED))

        testPinnedLabelVisibility()
    }

    private fun testPinnedLabelInvisibility() {
        composeTestRule.onNodeWithText(posts[0].content).assertExists()
        composeTestRule.onNodeWithText(context.getString(R.string.post_is_pinned))
            .assertDoesNotExist()
    }

    private fun testPinnedLabelVisibility() {
        composeTestRule.onNodeWithText(posts[0].content).assertExists()
        composeTestRule.onNodeWithText(context.getString(R.string.post_is_pinned))
            .assertExists()
    }

    private fun testPinDestination(changedPost: IBasePost?) {
        assert(changedPost != null)
        assert(changedPost is PostPojo)
        assert((changedPost as PostPojo).content == posts[0].content)
    }
}