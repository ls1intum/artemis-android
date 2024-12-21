package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.ext.junit.runners.AndroidJUnit4
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

@Category(UnitTest::class)
@RunWith(AndroidJUnit4::class)
class ConversationBottomSheetUiTest : BaseChatUITest() {

    private val currentUser = User(id = clientId)
    private val otherUser = User(id = 1234)

    private val postContent = "Post content"
    private val answerContent = "Answer content"

    @Test
    fun `test GIVEN a post WHEN long pressing the post THEN Edit action is shown`() {
        setupChatUi(
            posts = listOf(StandalonePost(
                id = 1,
                author = currentUser,
                content = postContent,
            )),
            currentUser = currentUser
        )

        composeTestRule.assertPostActionVisibility(R.string.post_edit, isVisible = true)
    }


    @Test
    fun `test GIVEN a user with moderation-rights WHEN long pressing the post THEN Edit action is not shown`() {
        setupChatUi(
            posts = listOf(StandalonePost(
                id = 1,
                author = otherUser,
                content = postContent,
            )),
            currentUser = currentUser,
            hasModerationRights = true
        )

        composeTestRule.assertPostActionVisibility(R.string.post_edit, isVisible = false)
    }

    @Test
    fun `test GIVEN a user with moderation-rights WHEN long pressing the post THEN delete option is shown`() {
        setupChatUi(
            posts = listOf(StandalonePost(
                id = 1,
                author = otherUser,
                content = postContent,
            )),
            currentUser = currentUser,
            hasModerationRights = true
        )

        composeTestRule.assertPostActionVisibility(R.string.post_delete, isVisible = true)
    }

    @Test
    fun `test GIVEN a post WHEN long pressing the post as the post author THEN delete option is shown`() {
        setupChatUi(
            posts = listOf(StandalonePost(
                id = 1,
                author = currentUser,
                content = postContent,
            )),
            currentUser = currentUser
        )

        composeTestRule.assertPostActionVisibility(R.string.post_delete, isVisible = true)
    }

    @Test
    fun `test GIVEN a post WHEN long pressing the post as non-moderator THEN delete option is not shown`() {
        setupChatUi(
            posts = listOf(StandalonePost(
                id = 1,
                author = otherUser,
                content = postContent,
            )),
            currentUser = currentUser
        )

        composeTestRule.assertPostActionVisibility(R.string.post_delete, isVisible = false)
    }

    @Test
    fun `test GIVEN a basePost from user WHEN long pressing on another user's answer THEN resolve option is shown`() {
        val answerContent = "Answer content"
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
        val answerContent = "Answer content"
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
        val answerContent = "Answer content"
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

    private fun simpleThreadPostWithAnswer(
        postAuthor: User,
        answerAuthor: User,
    ): StandalonePost {
        val basePost = StandalonePost(
            id = 1,
            author = postAuthor,
            content = postContent,
        )
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