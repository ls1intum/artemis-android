package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.TEST_TAG_DELETED_FORWARDED_POST
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.TEST_TAG_FORWARDED_POST_COLUMN
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.testTagForForwardedPost
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
class ConversationMessagesUITest : BaseChatUITest() {

    private fun testTagForPost(postId: StandalonePostId?) = "post$postId"

    // ################################# PIN VISIBILITY TESTS #####################################

    @Ignore("There is an open issue about onClick events not working for the ModalBottomSheetLayout with" +
            "the robolectric test runner. Enable this test again as soon as the following issue is resolved:" +
            "https://github.com/robolectric/robolectric/issues/9595")
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

    @Ignore("There is an open issue about onClick events not working for the ModalBottomSheetLayout with" +
            "the robolectric test runner. Enable this test again as soon as the following issue is resolved:" +
            "https://github.com/robolectric/robolectric/issues/9595")
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

    @Ignore("There is an open issue about onClick events not working for the ModalBottomSheetLayout with" +
            "the robolectric test runner. Enable this test again as soon as the following issue is resolved:" +
            "https://github.com/robolectric/robolectric/issues/9595")
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

    @Ignore("There is an open issue about onClick events not working for the ModalBottomSheetLayout with" +
            "the robolectric test runner. Enable this test again as soon as the following issue is resolved:" +
            "https://github.com/robolectric/robolectric/issues/9595")
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

    @Ignore("There is an open issue about onClick events not working for the ModalBottomSheetLayout with" +
            "the robolectric test runner. Enable this test again as soon as the following issue is resolved:" +
            "https://github.com/robolectric/robolectric/issues/9595")
    @Test
    fun `test GIVEN the post is not pinned THEN the post is not shown as pinned`() {
        setupChatUi(posts)

        testPinnedLabelInvisibility()
    }

    @Ignore("There is an open issue about onClick events not working for the ModalBottomSheetLayout with" +
            "the robolectric test runner. Enable this test again as soon as the following issue is resolved:" +
            "https://github.com/robolectric/robolectric/issues/9595")
    @Test
    fun `test GIVEN the post is not pinned in thread THEN the post is not shown as pinned in thread`() {
        setupThreadUi(posts[0])

        testPinnedLabelInvisibility()
    }

    @Ignore("There is an open issue about onClick events not working for the ModalBottomSheetLayout with" +
            "the robolectric test runner. Enable this test again as soon as the following issue is resolved:" +
            "https://github.com/robolectric/robolectric/issues/9595")
    @Test
    fun `test GIVEN the post is pinned THEN the post is shown as pinned`() {
        val modifiedPosts = posts.toMutableList()
        modifiedPosts[0] = modifiedPosts[0].copy(displayPriority = DisplayPriority.PINNED)
        setupChatUi(modifiedPosts)

        testPinnedLabelVisibility()
    }

    @Ignore("There is an open issue about onClick events not working for the ModalBottomSheetLayout with" +
            "the robolectric test runner. Enable this test again as soon as the following issue is resolved:" +
            "https://github.com/robolectric/robolectric/issues/9595")
    @Test
    fun `test GIVEN the post is pinned in thread THEN the post is shown as pinned in thread`() {
        setupThreadUi(posts[0].copy(displayPriority = DisplayPriority.PINNED))

        testPinnedLabelVisibility()
    }

    // ########################## FORWARDED POSTS VISIBILITY TESTS ##############################

    @Test
    fun `test GIVEN the post has a forwarded message THEN the forwarded message is shown below`() {
        setupChatUi(posts)

        testForwardedMessageVisibility()
    }

    @Test
    fun `test GIVEN no post has a forwarded message THEN no forwarded message is shown`() {
        val modifiedPosts = posts.toMutableList()
        modifiedPosts[1] = modifiedPosts[1].copy(hasForwardedMessages = false)
        setupChatUi(modifiedPosts)

        testForwardedMessageInvisibility()
    }

    @Test
    fun `test GIVEN the post has a forwarded message in a thread THEN the forwarded message is shown below`() {
        setupThreadUi(posts[1])

        testForwardedMessageVisibility()
    }

    @Test
    fun `test GIVEN the post has a forwarded message with a deleted source post THEN the message deleted indication is shown instead`() {
        setupChatUi(
            posts = posts,
            forwardedPosts = listOf(null)
        )

        composeTestRule.onNodeWithTag(TEST_TAG_DELETED_FORWARDED_POST, useUnmergedTree = true)
            .assertExists()
    }

    // ##########################################################################################

    private fun testForwardedMessageInvisibility() {
        composeTestRule.onNodeWithTag(TEST_TAG_FORWARDED_POST_COLUMN, useUnmergedTree = true)
            .assertDoesNotExist()
        composeTestRule.onNodeWithTag(testTagForForwardedPost(forwardedPosts[0].serverPostId), useUnmergedTree = true)
            .assertDoesNotExist()
    }

    private fun testForwardedMessageVisibility() {
        composeTestRule.onNodeWithTag(TEST_TAG_FORWARDED_POST_COLUMN, useUnmergedTree = true)
            .performScrollTo()
            .assertExists()
        composeTestRule.onNodeWithTag(
            testTagForForwardedPost(forwardedPosts[0].serverPostId),
            useUnmergedTree = true
        )
            .assertExists()
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