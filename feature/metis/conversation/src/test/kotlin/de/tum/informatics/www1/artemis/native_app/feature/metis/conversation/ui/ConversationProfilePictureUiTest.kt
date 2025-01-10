package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.data.AccountDataServiceStub
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.AccountDataService
import de.tum.informatics.www1.artemis.native_app.core.model.account.Account
import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import de.tum.informatics.www1.artemis.native_app.core.test.coreTestModules
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.LocalArtemisImageProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.test.ArtemisImageProviderStub
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.MockVisibleMetisContextManager
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.TestInitialReplyTextProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.impl.EmojiServiceStub
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.ChatListItem
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.MetisChatList
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.PostsDataState
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions.PostActionFlags
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IStandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.sharedConversationModule
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.TEST_TAG_PROFILE_PICTURE_IMAGE
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.TEST_TAG_PROFILE_PICTURE_INITIALS
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.TEST_TAG_PROFILE_PICTURE_UNKNOWN
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.TEST_TAG_USER_PROFILE_DIALOG
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.TEST_TAG_USER_PROFILE_DIALOG_SEND_MESSAGE
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter.LocalVisibleMetisContextManager
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter.VisiblePostList
import kotlinx.coroutines.CompletableDeferred
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.koin.test.KoinTestRule
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog


@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class ConversationProfilePictureUiTest : BaseComposeTest() {

    private val clientId = 20L
    private val courseId = 1L

    private val allTestTags = listOf(
        TEST_TAG_PROFILE_PICTURE_IMAGE,
        TEST_TAG_PROFILE_PICTURE_INITIALS,
        TEST_TAG_PROFILE_PICTURE_UNKNOWN
    )

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        androidContext(context)

        modules(coreTestModules)
        modules(sharedConversationModule)
        modules(module {
            single<AccountDataService> { AccountDataServiceStub(Account(id = clientId)) }
        })
    }

    @Before
    fun setUp() {
        ShadowLog.stream = System.out
        ArtemisImageProviderStub.setup(context)
    }

    @Test
    fun `test GIVEN a post with an authorImageUrl WHEN displaying the post THEN the profile picture is shown`() {
        val post = StandalonePost(
            id = 1L,
            author = User(
                id = 1L,
                name = "author",
                imageUrl = "test.png"
            ),
        )
        setupUi(post)

        composeTestRule.assertTestTagExclusivelyExists(
            exclusiveTag = TEST_TAG_PROFILE_PICTURE_IMAGE,
            allTags = allTestTags
        )
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

    @Test
    fun `test GIVEN a post with a profile picture WHEN clicking on it THEN the user profile dialog is shown and sendMessage button is shown`() {
        val post = StandalonePost(
            id = 1L,
            author = User(
                id = 1L,
                name = "author",
                imageUrl = null
            ),
        )
        setupUi(post)

        composeTestRule.onNodeWithTag(TEST_TAG_PROFILE_PICTURE_INITIALS)
            .performClick()

        composeTestRule.onNodeWithTag(TEST_TAG_USER_PROFILE_DIALOG)
            .assertExists()

        composeTestRule.onNodeWithTag(TEST_TAG_USER_PROFILE_DIALOG_SEND_MESSAGE)
            .assertExists()
    }

    @Test
    fun `test GIVEN a post by the current user WHEN displaying the user dialog THEN the sendMessage button is not shown`() {
        val post = StandalonePost(
            id = 1L,
            author = User(
                id = clientId,
                name = "current user",
                imageUrl = null
            ),
        )
        setupUi(post)

        composeTestRule.onNodeWithTag(TEST_TAG_PROFILE_PICTURE_INITIALS)
            .performClick()

        composeTestRule.onNodeWithTag(TEST_TAG_USER_PROFILE_DIALOG)
            .assertExists()

        composeTestRule.onNodeWithTag(TEST_TAG_USER_PROFILE_DIALOG_SEND_MESSAGE)
            .assertDoesNotExist()
    }


    private fun setupUi(
        post: IStandalonePost
    ) {
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalArtemisImageProvider provides ArtemisImageProviderStub(),
                LocalVisibleMetisContextManager provides MockVisibleMetisContextManager.also {
                    it.registerMetisContext(VisiblePostList(MetisContext.Course(courseId)))
                }
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
                    postActionFlags = PostActionFlags(
                        isAbleToPin = false,
                        isAtLeastTutorInCourse = false,
                        hasModerationRights = false
                    ),
                    onPinPost = { CompletableDeferred() },
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
                    onFileSelected =  { _ -> }
                )
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