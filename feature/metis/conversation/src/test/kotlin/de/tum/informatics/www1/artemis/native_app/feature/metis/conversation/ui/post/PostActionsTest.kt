package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post

import androidx.compose.runtime.Composable
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith

@Category(UnitTest::class)
@RunWith(AndroidJUnit4::class)
class PostActionsTest : BaseComposeTest() {

    private val EDIT_POST = {}

    private val author = User(id = 1)
    private val instructor = User(id = 2)

    private val post = StandalonePost(
        id = 1,
        author = author
    )

    @Test
    fun `test GIVEN a post WHEN calling rememberPostActions as the post author THEN onRequestEditPost is not null`() {
        composeTestRule.setContent {
            val postActions = createPostActions(
                editorId = author.id,
                hasModerationRights = false
            )

            assertNotNull(postActions.requestEditPost)
            assertEquals(EDIT_POST, postActions.requestEditPost)
        }
    }


    @Test
    fun `test GIVEN a user with moderation-rights WHEN calling rememberPostActions THEN onRequestEditPost is null`() {
        composeTestRule.setContent {
            val postActions = createPostActions(
                editorId = instructor.id,
                hasModerationRights = true
            )

            assertNull(postActions.requestEditPost)
        }
    }


    @Composable
    private fun createPostActions(
        editorId: Long,
        hasModerationRights: Boolean = false,
    ): PostActions {
        val postActions = rememberPostActions(
            post = post,
            hasModerationRights = hasModerationRights,
            isAtLeastTutorInCourse = false,
            clientId = editorId,
            onRequestEdit = EDIT_POST,
            onRequestDelete = {},
            onClickReaction = { _, _ -> },
            onReplyInThread = {},
            onResolvePost = {},
            onRequestRetrySend = {}
        )
        return postActions
    }
}