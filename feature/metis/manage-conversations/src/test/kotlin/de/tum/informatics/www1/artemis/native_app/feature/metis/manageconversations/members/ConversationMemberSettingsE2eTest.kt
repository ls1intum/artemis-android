package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.members

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.SavedStateHandle
import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.common.test.testServerUrl
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.CollapsingContentState
import de.tum.informatics.www1.artemis.native_app.feature.login.test.user1DisplayName
import de.tum.informatics.www1.artemis.native_app.feature.login.test.user1Username
import de.tum.informatics.www1.artemis.native_app.feature.login.test.user2DisplayName
import de.tum.informatics.www1.artemis.native_app.feature.login.test.user2Username
import de.tum.informatics.www1.artemis.native_app.feature.login.test.user3DisplayName
import de.tum.informatics.www1.artemis.native_app.feature.login.test.user3Username
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.members.ConversationMembersBody
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.members.ConversationMembersViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.members.TEST_TAG_MEMBERS_LIST
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.members.testTagForMember
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metistest.ConversationBaseTest
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.test.get
import org.robolectric.RobolectricTestRunner
import kotlin.time.Duration.Companion.seconds
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.R as R_shared

@OptIn(ExperimentalTestApi::class)
@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
class ConversationMemberSettingsE2eTest : ConversationBaseTest() {

    private lateinit var channel: ChannelChat

    private val isModeratorCheck = hasContentDescription(
        context.getString(R_shared.string.user_role_moderator)
    )

    override fun setup() {
        super.setup()

        channel = runBlockingWithTestTimeout {
            conversationService.createChannel(
                courseId = course.id!!,
                name = "test-channel",
                description = "",
                isPublic = true,
                isAnnouncement = true,
                authToken = accessToken,
                serverUrl = testServerUrl
            )
                .orThrow("could not create group chat")
                .also { channelChat ->
                    conversationService.registerMembers(
                        courseId = course.id!!,
                        conversation = channelChat,
                        users = listOf(user2Username, user3Username),
                        authToken = accessToken,
                        serverUrl = testServerUrl
                    )
                        .orThrow("Could not register additional members")
                }
        }
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `displays conversation members`() {
        setupUiAndViewModel()

        composeTestRule
            .onNodeWithTag(testTagForMember(user1Username))
            .performScrollTo()
            .assert(hasText(user1DisplayName))
            .assert(isModeratorCheck)

        composeTestRule
            .onNodeWithTag(testTagForMember(user2Username))
            .performScrollTo()
            .assert(
                hasText(user2DisplayName) and !isModeratorCheck
            )

        composeTestRule
            .onNodeWithTag(testTagForMember(user3Username))
            .performScrollTo()
            .assert(
                hasText(user3DisplayName) and !isModeratorCheck
            )
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can promote user to moderator`() {
        setupUiAndViewModel()

        changeModeratorStatusTestImpl(true)
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can revoke moderation rights`() {
        runBlockingWithTestTimeout {
            conversationService.grantModerationRights(
                course.id!!,
                channel,
                user2Username,
                accessToken,
                testServerUrl
            ).orThrow("Could not promote user to moderator")
        }

        setupUiAndViewModel()

        changeModeratorStatusTestImpl(false)
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can kick member`() {
        setupUiAndViewModel()

        composeTestRule
            .onNode(
                hasAnyAncestor(hasTestTag(testTagForMember(user2Username))) and
                        hasContentDescription(
                            context.getString(R.string.conversation_members_content_description_kick_user)
                        )
            )
            .performScrollTo()
            .performClick()

        composeTestRule
            .onNodeWithText(
                context.getString(R.string.conversation_settings_dialog_kick_member_positive)
            )
            .performClick()

        composeTestRule.waitUntilDoesNotExist(
            hasTestTag(testTagForMember(user2Username)),
            DefaultTimeoutMillis
        )
    }

    private fun changeModeratorStatusTestImpl(makeModerator: Boolean) {
        composeTestRule
            .onNode(
                hasAnyAncestor(hasTestTag(testTagForMember(user2Username))) and
                        hasContentDescription(
                            context.getString(
                                if (makeModerator) {
                                    R.string.conversation_members_content_description_add_moderator
                                } else {
                                    R.string.conversation_members_content_description_remove_moderator
                                }
                            )
                        )
            )
            .performScrollTo()
            .performClick()

        composeTestRule
            .onNodeWithText(
                context.getString(
                    if (makeModerator) R.string.conversation_settings_dialog_grant_moderation_rights_positive
                    else R.string.conversation_settings_dialog_revoke_moderation_rights_positive
                )
            )
            .performClick()

        composeTestRule.waitUntilExactlyOneExists(
            hasTestTag(testTagForMember(user2Username)) and if (makeModerator) isModeratorCheck else !isModeratorCheck,
            DefaultTimeoutMillis
        )
    }

    private fun setupUiAndViewModel() {
        val viewModel = ConversationMembersViewModel(
            initialCourseId = course.id!!,
            initialConversationId = channel.id,
            conversationService = get(),
            accountService = get(),
            serverConfigurationService = get(),
            networkStatusProvider = get(),
            accountDataService = get(),
            savedStateHandle = SavedStateHandle(),
            coroutineContext = testDispatcher
        )

        composeTestRule.setContent {
            ConversationMembersBody(
                modifier = Modifier.fillMaxSize(),
                courseId = course.id!!,
                conversationId = channel.id,
                viewModel = viewModel,
                collapsingContentState = CollapsingContentState()
            )
        }

        testDispatcher.scheduler.advanceTimeBy(2.seconds)

        composeTestRule.waitUntilExactlyOneExists(
            hasTestTag(TEST_TAG_MEMBERS_LIST),
            DefaultTimeoutMillis
        )
    }
}