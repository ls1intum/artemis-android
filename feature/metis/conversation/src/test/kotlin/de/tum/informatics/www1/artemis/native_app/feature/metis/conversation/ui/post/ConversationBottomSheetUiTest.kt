package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.BaseChatUITest
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions.TEST_TAG_POST_CONTEXT_BOTTOM_SHEET
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions.TEST_TAG_POST_REACTIONS_BOTTOM_SHEET
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions.getTestTagForEmojiId
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions.getTestTagForReactionAuthor
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import org.junit.Ignore
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class ConversationBottomSheetUiTest : BaseChatUITest() {

    // ###################################### EDIT ###########################################

    @Test
    fun `test GIVEN a post WHEN long pressing the post THEN Edit action is shown`() {
        setupChatUi(
            posts = listOf(simplePost(currentUser)),
        )

        composeTestRule.assertPostActionVisibility(R.string.post_edit, isVisible = true)
    }


    @Test
    fun `test GIVEN a user with moderation-rights WHEN long pressing the other's post THEN Edit action is not shown`() {
        setupChatUi(
            posts = listOf(simplePost(otherUser)),
            hasModerationRights = true
        )

        composeTestRule.assertPostActionVisibility(R.string.post_edit, isVisible = false)
    }


    // ###################################### DELETE ###########################################

    @Test
    fun `test GIVEN a user with moderation-rights WHEN long pressing other's post THEN delete option is shown`() {
        setupChatUi(
            posts = listOf(simplePost(otherUser)),
            hasModerationRights = true
        )

        composeTestRule.assertPostActionVisibility(R.string.post_delete, isVisible = true)
    }

    @Test
    fun `test GIVEN a post WHEN long pressing the post as the post author THEN delete option is shown`() {
        setupChatUi(
            posts = listOf(simplePost(currentUser)),
        )

        composeTestRule.assertPostActionVisibility(R.string.post_delete, isVisible = true)
    }

    @Test
    fun `test GIVEN a post WHEN long pressing the other's post as non-moderator THEN delete option is not shown`() {
        setupChatUi(
            posts = listOf(simplePost(otherUser)),
        )

        composeTestRule.assertPostActionVisibility(R.string.post_delete, isVisible = false)
    }


    // ###################################### RESOLVE ###########################################

    @Test
    fun `test GIVEN a basePost from user WHEN long pressing on another user's answer THEN resolve option is shown`() {
        setupThreadUi(
            post = simpleThreadPostWithAnswer(
                postAuthor = currentUser,
                answerAuthor = otherUser
            )
        )

        composeTestRule.assertPostActionVisibility(
            R.string.post_resolves,
            isVisible = true,
            simpleAnswerPostId
        )
    }

    @Test
    fun `test GIVEN a basePost from another user WHEN long pressing on another user's answer as tutor THEN resolve option is shown`() {
        setupThreadUi(
            post = simpleThreadPostWithAnswer(
                postAuthor = otherUser,
                answerAuthor = otherUser
            ),
            isAtLeastTutorInCourse = true
        )

        composeTestRule.assertPostActionVisibility(
            R.string.post_resolves,
            isVisible = true,
            simpleAnswerPostId
        )
    }

    @Test
    fun `test GIVEN a basePost from another user WHEN long pressing on a users answer THEN resolve option is not shown`() {
        setupThreadUi(
            post = simpleThreadPostWithAnswer(
                postAuthor = otherUser,
                answerAuthor = currentUser,
            )
        )

        composeTestRule.assertPostActionVisibility(
            R.string.post_resolves,
            isVisible = false,
            simpleAnswerPostId
        )
    }


    // ###################################### PIN ###########################################

    @Test
    fun `test GIVEN other's post WHEN long pressing with pin rights THEN pin option is shown`() {
        setupChatUi(
            posts = listOf(StandalonePost(
                id = 1,
                author = otherUser,
                content = simplePostContent,
            )),
            isAbleToPin = true
        )

        composeTestRule.assertPostActionVisibility(R.string.post_pin, isVisible = true)
    }

    @Test
    fun `test GIVEN a post WHEN long pressing without pin rights THEN pin option is not shown`() {
        setupChatUi(
            posts = listOf(simplePost(otherUser)),
        )

        composeTestRule.assertPostActionVisibility(R.string.post_pin, isVisible = false)
    }

    @Test
    fun `test GIVEN a answer to a post WHEN long pressing the answer with pin abilities THEN pin option is not shown`() {
        setupThreadUi(
            post = simpleThreadPostWithAnswer(
                postAuthor = currentUser,
                answerAuthor = currentUser
            ),
            isAbleToPin = true
        )

        composeTestRule.assertPostActionVisibility(
            R.string.post_pin,
            isVisible = false,
            simpleAnswerPostId
        )
    }

    // ###################################### UTIL METHODS ###########################################

    @Test
    fun `test GIVEN a post WHEN long pressing the post as non-moderator THEN save option is shown`() {
        setupChatUi(
            posts = listOf(simplePost(otherUser))
        )

        composeTestRule.assertPostActionVisibility(R.string.post_save, isVisible = true)
    }

    @Test
    fun `test GIVEN a saved post WHEN long pressing the post as non-moderator THEN un-save option is shown`() {
        setupChatUi(
            posts = listOf(simplePost(otherUser, isSaved = true)),
        )

        composeTestRule.assertPostActionVisibility(R.string.post_unsave, isVisible = true)
    }

    // ######################################## FORWARD ##############################################

    @Test
    fun `test GIVEN a post that has content WHEN long pressing the post THEN forward option is shown`() {
        setupChatUi(
            posts = listOf(simplePost(otherUser))
        )

        composeTestRule.assertPostActionVisibility(R.string.post_forward, isVisible = true)
    }

    @Ignore("This test is flaky and does not work")
    @Test
    fun `test GIVEN a post that has no content but forwarded messages WHEN long pressing the post THEN forward option is shown`() {
        val post = posts[1].copy(content = "")
        setupChatUi(
            posts = listOf(post)
        )

        val postId = post.serverPostId ?: post.hashCode().toLong()
        composeTestRule.assertPostActionVisibility(R.string.post_forward, isVisible = true, postId)
    }

    @Test
    fun `test GIVEN a post that no content and a deleted forwarded message WHEN long pressing the post THEN forward option is not shown`() {
        setupChatUi(
            posts = listOf(posts[1].copy(content = "")),
            forwardedPosts = listOf(null)
        )

        val postId = posts[1].serverPostId ?: posts[1].hashCode().toLong()
        composeTestRule.assertPostActionVisibility(R.string.post_forward, isVisible = false, postId)
    }

    // ################################## VIEW REACTING AUTHORS ######################################

    @Test
    fun `test GIVEN a post with reactions WHEN long pressing a reaction THEN at least this reaction is shown`() {
        setupChatUi(
            posts = posts
        )
        val reactionToClick = reactions.first().emojiId
        val expectedReaction = posts[0].reactions.first().emojiId

        composeTestRule.assertPostReactionVisibility(
            emojiId = reactionToClick,
            testTag = getTestTagForEmojiId(expectedReaction, "REACTIONS_BOTTOM_SHEET")
        )
    }

    @Test
    fun `test GIVEN a post with reactions WHEN long pressing a reaction THEN the reacting user is shown correctly`() {
       setupChatUi(
            posts = posts
        )
        val reactionToView = posts[0].reactions.last().emojiId
        val expectedReactionEmojiId = reactions.last().emojiId
        val expectedReactionId = reactions.last().id
        val expectedReactionUsername = reactions.last().username

        composeTestRule.assertPostReactionVisibility(
            emojiId = reactionToView,
            testTag = getTestTagForReactionAuthor(expectedReactionEmojiId, expectedReactionId, expectedReactionUsername)
        )
    }

    @Test
    fun `test GIVEN a post with reactions WHEN long pressing a reaction THEN users reacting with another reaction are hidden`() {
        setupChatUi(
            posts = posts
        )
        val reactionToView = posts[0].reactions.last().emojiId
        val expectedHiddenUsername = reactions.first().username
        val expectedHiddenReactionId = reactions.first().id
        val expectedHiddenReactionEmojiId = reactions.first().emojiId

        composeTestRule.assertPostReactionVisibility(
            emojiId = reactionToView,
            testTag = getTestTagForReactionAuthor(expectedHiddenReactionEmojiId, expectedHiddenReactionId, expectedHiddenUsername),
            isVisible = false
        )
    }


    private fun ComposeTestRule.assertPostActionVisibility(
        stringResId: Int,
        isVisible: Boolean,
        id: Long = simplePostId,
    ) {
        onNodeWithTag(getTestTagForPost(id))
            .performSemanticsAction(SemanticsActions.OnLongClick)

        onNodeWithTag(TEST_TAG_POST_CONTEXT_BOTTOM_SHEET)
            .assertIsDisplayed()

        val actionNode = onNodeWithText(context.getString(stringResId))
        if (isVisible) {
            actionNode.assertExists().assertIsDisplayed()
        } else {
            actionNode.assertDoesNotExist()
        }
    }

    private fun ComposeTestRule.assertPostReactionVisibility(
        emojiId: String,
        isVisible: Boolean = true,
        testTag: String = "",
    ) {
        onNodeWithTag(getTestTagForEmojiId(emojiId, "POST_ITEM"))
            .performSemanticsAction(SemanticsActions.OnLongClick)

        onNodeWithTag(TEST_TAG_POST_REACTIONS_BOTTOM_SHEET)
            .assertIsDisplayed()

        val visibleNode = onNodeWithTag(testTag)

        if (isVisible) {
            visibleNode.assertExists().assertIsDisplayed()
        } else {
            visibleNode.assertDoesNotExist()
        }
    }
}