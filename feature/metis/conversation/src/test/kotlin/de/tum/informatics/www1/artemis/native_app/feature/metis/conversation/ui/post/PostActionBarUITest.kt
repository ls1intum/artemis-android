package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.BaseChatUITest
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions.TEST_TAG_PIN_POST
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions.TEST_TAG_POST_DELETE
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions.TEST_TAG_POST_EDIT
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions.TEST_TAG_SAVE_POST
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.UserRole
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith

@Category(UnitTest::class)
@RunWith(AndroidJUnit4::class)
class PostActionBarUITest: BaseChatUITest() {

    private val otherUserId = 1234L
    private val otherUserName = "Other User"
    private val otherUserRole = UserRole.USER

    @Test
    fun `test GIVEN a post WHEN navigating to the thread view THEN Edit action is shown`() {
        setupThreadUi(
            post = posts[0],
            hasModerationRights = false,
        )

        composeTestRule.onNodeWithTag(TEST_TAG_POST_EDIT).assertExists().assertIsDisplayed()
    }

    @Test
    fun `test GIVEN a user with moderation-rights WHEN navigating to the thread view THEN Edit action is not shown`() {
        setupThreadUi(
            post = posts[0].copy(
                authorId = otherUserId,
                authorName = otherUserName,
                authorRole = otherUserRole
            ),
            hasModerationRights = true,
        )

        composeTestRule.onNodeWithTag(TEST_TAG_POST_EDIT).assertDoesNotExist()
    }

    @Test
    fun `test GIVEN a user with moderation-rights WHEN navigating to the thread view THEN delete option is shown`() {
        setupThreadUi(
            post = posts[0].copy(
                authorId = otherUserId,
                authorName = otherUserName,
                authorRole = otherUserRole
            ),
            hasModerationRights = true,
        )

        composeTestRule.onNodeWithTag(TEST_TAG_POST_DELETE).assertExists().assertIsDisplayed()
    }

    @Test
    fun `test GIVEN a post WHEN navigating to the thread view as the post author THEN delete option is shown`() {
        setupThreadUi(
            post = posts[0]
        )

        composeTestRule.onNodeWithTag(TEST_TAG_POST_DELETE).assertExists().assertIsDisplayed()
    }

    @Test
    fun `test GIVEN a post WHEN navigating to the thread view as non-moderator THEN delete option is not shown`() {
        setupThreadUi(
            post = posts[0].copy(
                authorId = otherUserId,
                authorName = otherUserName,
                authorRole = otherUserRole
            ),
            hasModerationRights = false,
        )

        composeTestRule.onNodeWithTag(TEST_TAG_POST_DELETE).assertDoesNotExist()
    }

    @Test
    fun `test GIVEN a post WHEN navigating to the thread view with pin abilities THEN pin option is shown`() {
        setupThreadUi(
            post = posts[0].copy(
                authorId = otherUserId,
                authorName = otherUserName,
                authorRole = otherUserRole
            ),
            isAbleToPin = true,
        )

        composeTestRule.onNodeWithTag(TEST_TAG_PIN_POST).assertExists().assertIsDisplayed()
    }

    @Test
    fun `test GIVEN a post WHEN navigating to the thread view without pin abilities THEN pin option is not shown`() {
        setupThreadUi(
            post = posts[0],
            isAbleToPin = false,
        )

        composeTestRule.onNodeWithTag(TEST_TAG_PIN_POST).assertDoesNotExist()
    }

    @Test
    fun `test GIVEN a post WHEN navigating to the thread view THEN save option is shown`() {
        setupThreadUi(
            post = posts[0]
        )

        composeTestRule.onNodeWithTag(TEST_TAG_SAVE_POST).assertExists().assertIsDisplayed()
    }
}