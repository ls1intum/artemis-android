package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.data.test.AccountDataServiceStub
import de.tum.informatics.www1.artemis.native_app.core.model.account.Account
import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.impl.ArtemisImageProviderStub
import de.tum.informatics.www1.artemis.native_app.device.test.NetworkStatusProviderStub
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.BaseChatUITest
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.TEST_TAG_PROFILE_PICTURE_IMAGE
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.TEST_TAG_PROFILE_PICTURE_INITIALS
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.TEST_TAG_PROFILE_PICTURE_UNKNOWN
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.TEST_TAG_USER_PROFILE_DIALOG
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.TEST_TAG_USER_PROFILE_DIALOG_SEND_MESSAGE
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.UserProfileDialogViewModel
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
class ConversationProfilePictureUiTest : BaseChatUITest() {

    private val allTestTags = listOf(
        TEST_TAG_PROFILE_PICTURE_IMAGE,
        TEST_TAG_PROFILE_PICTURE_INITIALS,
        TEST_TAG_PROFILE_PICTURE_UNKNOWN
    )

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        androidContext(context)

        modules(module {
            single<UserProfileDialogViewModel> { params ->
                UserProfileDialogViewModel(
                    courseId = params[0],
                    userId = params[1],
                    accountDataService = AccountDataServiceStub(Account(id = clientId)),
                    networkStatusProvider = NetworkStatusProviderStub(),
                    coroutineContext = testDispatcher
                )
            }
        })
    }

    @Before
    fun setUp() {
        ShadowLog.stream = System.out
        ArtemisImageProviderStub.setup(context)
    }

    @Test
    fun `test GIVEN a post with an authorImageUrl WHEN displaying the post THEN the profile picture is shown`() {
        val post = simplePost(
            postAuthor = User(
                id = 1L,
                name = "author",
                imageUrl = "test.png"
            ),
        )
        setupChatUi(listOf(post))

        composeTestRule.assertTestTagExclusivelyExists(
            exclusiveTag = TEST_TAG_PROFILE_PICTURE_IMAGE,
            allTags = allTestTags
        )
    }

    @Test
    fun `test GIVEN a post without an authorImageUrl WHEN displaying the post THEN the users initials are shown`() {
        val post = simplePost(
            postAuthor = User(
                id = 1L,
                name = "author",
                imageUrl = null
            ),
        )
        setupChatUi(listOf(post))

        composeTestRule.assertTestTagExclusivelyExists(
            exclusiveTag = TEST_TAG_PROFILE_PICTURE_INITIALS,
            allTags = allTestTags
        )
    }

    @Test
    fun `test GIVEN a post with null for userName WHEN displaying the post THEN the unknown profile picture is shown`() {
        val post = simplePost(
            postAuthor = User(
                id = 1L,
                name = null,
                imageUrl = null
            ),
        )
        setupChatUi(listOf(post))

        composeTestRule.assertTestTagExclusivelyExists(
            exclusiveTag = TEST_TAG_PROFILE_PICTURE_UNKNOWN,
            allTags = allTestTags
        )
    }

    @Test
    fun `test GIVEN a post with a profile picture WHEN clicking on it THEN the user profile dialog is shown and sendMessage button is shown`() {
        val post = simplePost(postAuthor = otherUser)
        setupChatUi(listOf(post))

        composeTestRule.onNodeWithTag(TEST_TAG_PROFILE_PICTURE_INITIALS)
            .performClick()

        composeTestRule.onNodeWithTag(TEST_TAG_USER_PROFILE_DIALOG)
            .assertExists()

        composeTestRule.onNodeWithTag(TEST_TAG_USER_PROFILE_DIALOG_SEND_MESSAGE)
            .assertExists()
    }

    @Test
    fun `test GIVEN a post by the current user WHEN displaying the user dialog THEN the sendMessage button is not shown`() {
        val post = simplePost(postAuthor = currentUser)
        setupChatUi(listOf(post))

        composeTestRule.onNodeWithTag(TEST_TAG_PROFILE_PICTURE_INITIALS)
            .performClick()

        composeTestRule.onNodeWithTag(TEST_TAG_USER_PROFILE_DIALOG)
            .assertExists()

        composeTestRule.onNodeWithTag(TEST_TAG_USER_PROFILE_DIALOG_SEND_MESSAGE)
            .assertDoesNotExist()
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