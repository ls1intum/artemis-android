package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.TestInitialReplyTextProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.impl.EmojiServiceStub
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.ChatListItem
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.MetisChatList
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.PostsDataState
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.UserRole
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.PostPojo
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.TEST_TAG_PROFILE_PICTURE_IMAGE
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.TEST_TAG_PROFILE_PICTURE_INITIALS
import de.tum.informatics.www1.artemis.native_app.feature.metistest.ProfilePictureImageProviderStub
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class ConversationProfilePictureUiTest : BaseComposeTest() {

    private val clientId = 20L
    private val courseId = 1L

    private var profilePictureImageProvider = ProfilePictureImageProviderStub()

    private val postWithAuthorImageUrl = PostPojo(
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
        authorImageUrl = "authorImageUrl",
        courseWideContext = null,
        tags = emptyList(),
        answers = emptyList(),
        reactions = emptyList()
    )

    @Test
    fun `test GIVEN a post with an authorImageUrl WHEN displaying the post THEN the profile picture is shown`() = runTest {
        val post = postWithAuthorImageUrl
        setupUi(post)

        composeTestRule.waitUntilNoAssertionError {
            composeTestRule.onNodeWithTag(TEST_TAG_PROFILE_PICTURE_IMAGE, useUnmergedTree = true)
                .assertExists()
        }
    }

    @Test
    fun `test GIVEN a post without an authorImageUrl WHEN displaying the post THEN the users initials are shown`() {
        val post = postWithAuthorImageUrl.copy(authorImageUrl = null)
        setupUi(post)

        composeTestRule.onNodeWithTag(TEST_TAG_PROFILE_PICTURE_INITIALS, useUnmergedTree = true)
            .assertExists()
    }


    fun setupUi(
        post: PostPojo
    ) {
        composeTestRule.setContent {
            MetisChatList(
                modifier = Modifier,
                initialReplyTextProvider = TestInitialReplyTextProvider(),
                posts = PostsDataState.Loaded.WithList(
                    posts = listOf(ChatListItem.PostChatListItem(post)),
                    appendState = PostsDataState.NotLoading
                ),
                bottomItem = null,
                clientId = clientId,
                hasModerationRights = false,
                isAtLeastTutorInCourse = false,
                listContentPadding = PaddingValues(0.dp),
                serverUrl = "",
                courseId = courseId,
                state = LazyListState(),
                isReplyEnabled = false,
                profilePictureImageProvider = profilePictureImageProvider,
                emojiService = EmojiServiceStub,
                onCreatePost = { CompletableDeferred() },
                onEditPost = { _, _ -> CompletableDeferred() },
                onDeletePost = { CompletableDeferred() },
                onRequestReactWithEmoji = { _, _, _ -> CompletableDeferred() },
                onClickViewPost = {},
                onRequestRetrySend = {},
                imageLoaderCreation = { CompletableDeferred() },
                title = "title",
            )
        }

        composeTestRule.waitForIdle()
    }

    private fun ComposeTestRule.waitUntilNoAssertionError(
        timeoutMillis: Long = DefaultTimeoutMillis,
        assertion: () -> Unit,
    ) {
        waitUntil(timeoutMillis) {
            try {
                assertion()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }
}