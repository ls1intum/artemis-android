package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import coil.request.ImageRequest
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.ArtemisImageProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.LocalArtemisImageProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.TestInitialReplyTextProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.impl.EmojiServiceStub
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.ChatListItem
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.MetisChatList
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.PostsDataState
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IStandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.TEST_TAG_PROFILE_PICTURE_IMAGE
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.TEST_TAG_PROFILE_PICTURE_INITIALS
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.TEST_TAG_PROFILE_PICTURE_UNKNOWN
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class ConversationProfilePictureUiTest : BaseComposeTest() {

    private val clientId = 20L
    private val courseId = 1L

    private val artemisImageProviderStub = object : ArtemisImageProvider {
        private val sampleImageUrl = "https://picsum.photos/200"

        @Composable
        override fun rememberArtemisImageRequest(imagePath: String): ImageRequest {
            return ImageRequest.Builder(LocalContext.current)
                .data(sampleImageUrl)
                .size(coil.size.Size.ORIGINAL)
                .build()
        }
    }

    private val allTestTags = listOf(
        TEST_TAG_PROFILE_PICTURE_IMAGE,
        TEST_TAG_PROFILE_PICTURE_INITIALS,
        TEST_TAG_PROFILE_PICTURE_UNKNOWN
    )

    @Test
    fun `test GIVEN a post with an authorImageUrl WHEN displaying the post THEN the profile picture is shown`() = runTest {
        val post = StandalonePost(
            id = 1L,
            author = User(
                id = 1L,
                name = "author",
                imageUrl = "https://picsum.photos/200"
            ),
        )
        setupUi(post)

        composeTestRule.waitUntilNoAssertionError {
            composeTestRule.assertTestTagExclusivelyExists(
                exclusiveTag = TEST_TAG_PROFILE_PICTURE_IMAGE,
                allTags = allTestTags
            )
        }
    }

    @Test
    fun `test GIVEN a post without an authorImageUrl WHEN displaying the post THEN the users initials are shown`() {
        val post = StandalonePost(
            id = 1L,
            author = User(
                id = 1L,
                name = "author",
                imageUrl = null
            ),
        )
        setupUi(post)

        composeTestRule.assertTestTagExclusivelyExists(
            exclusiveTag = TEST_TAG_PROFILE_PICTURE_INITIALS,
            allTags = allTestTags
        )
    }

    @Test
    fun `test GIVEN a post with null for userName WHEN displaying the post THEN the unknown profile picture is shown`() {
        val post = StandalonePost(
            id = 1L,
            author = User(
                id = 1L,
                name = null,
                imageUrl = null
            ),
        )
        setupUi(post)

        composeTestRule.assertTestTagExclusivelyExists(
            exclusiveTag = TEST_TAG_PROFILE_PICTURE_UNKNOWN,
            allTags = allTestTags
        )
    }

    private fun setupUi(
        post: IStandalonePost
    ) {
        composeTestRule.setContent {
            CompositionLocalProvider(
               LocalArtemisImageProvider provides artemisImageProviderStub
            ) {
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
                    emojiService = EmojiServiceStub,
                    onCreatePost = { CompletableDeferred() },
                    onEditPost = { _, _ -> CompletableDeferred() },
                    onDeletePost = { CompletableDeferred() },
                    onRequestReactWithEmoji = { _, _, _ -> CompletableDeferred() },
                    onClickViewPost = {},
                    onRequestRetrySend = {},
                    title = "title",
                )
            }
        }
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

    private fun ComposeTestRule.assertTestTagExclusivelyExists(
        exclusiveTag: String,
        allTags: List<String>,
        useUnmergedTree: Boolean = true
    ) {
        allTags.filter { it != exclusiveTag }.forEach {
            onNodeWithTag(it, useUnmergedTree).assertDoesNotExist()
        }
        onNodeWithTag(exclusiveTag, useUnmergedTree).assertExists()
    }
}