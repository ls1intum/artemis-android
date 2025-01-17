package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.BaseChatUITest
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions.TEST_TAG_POST_CONTEXT_BOTTOM_SHEET
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.AnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class ConversationBottomSheetUiTest : BaseChatUITest() {

    private val currentUser = User(id = clientId)
    private val otherUser = User(id = 1234)

    private val postContent = "Post content"
    private val answerContent = "Answer content"

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
            postContentToClick = answerContent
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
            postContentToClick = answerContent
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
            postContentToClick = answerContent
        )
    }


    // ###################################### PIN ###########################################

    @Test
    fun `test GIVEN other's post WHEN long pressing with pin rights THEN pin option is shown`() {
        setupChatUi(
            posts = listOf(StandalonePost(
                id = 1,
                author = otherUser,
                content = postContent,
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
            postContentToClick = answerContent
        )
    }


    // ###################################### UTIL METHODS ###########################################

    private fun simplePost(
        postAuthor: User,
    ): StandalonePost = StandalonePost(
        id = 1,
        author = postAuthor,
        content = postContent,
    )

    private fun simpleThreadPostWithAnswer(
        postAuthor: User,
        answerAuthor: User,
    ): StandalonePost {
        val basePost = simplePost(postAuthor)
        val answerPost = AnswerPost(
            id = 2,
            author = answerAuthor,
            content = answerContent,
            post = basePost
        )
        return basePost.copy(answers = listOf(answerPost))
    }

    private fun ComposeTestRule.assertPostActionVisibility(
        stringResId: Int,
        isVisible: Boolean,
        postContentToClick: String = this@ConversationBottomSheetUiTest.postContent,
    ) {
        onNodeWithText(postContentToClick)
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

}