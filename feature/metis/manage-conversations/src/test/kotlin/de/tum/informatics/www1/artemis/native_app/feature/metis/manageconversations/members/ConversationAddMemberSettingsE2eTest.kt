package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.members

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.common.test.testServerUrl
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.feature.login.test.user1Username
import de.tum.informatics.www1.artemis.native_app.feature.login.test.user2Username
import de.tum.informatics.www1.artemis.native_app.feature.login.test.user3Username
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.common.ConversationUserSelectionScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.add_members.ConversationAddMembersViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.add_members.TEST_TAG_ADD_MEMBERS_BUTTON
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.member_selection.MemberSelectionBaseViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.member_selection.TEST_TAG_MEMBER_SELECTION_SEARCH_FIELD
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.member_selection.testTagForPotentialRecipient
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.member_selection.util.getTestTagForRecipient
import de.tum.informatics.www1.artemis.native_app.feature.metistest.ConversationBaseTest
import kotlinx.coroutines.test.TestDispatcher
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.test.get
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertContains

@OptIn(ExperimentalTestApi::class)
@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
class ConversationAddMemberSettingsE2eTest : ConversationBaseTest() {

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can add members to channel`() {
        val conversation = runBlockingWithTestTimeout {
            conversationService.createChannel(
                courseId = course.id!!,
                name = "testchannel",
                description = "",
                isPublic = true,
                isAnnouncement = true,
                isCourseWide = true,
                authToken = accessToken,
                serverUrl = testServerUrl
            ).orThrow("Could not create channel")
        }

        addRecipientsTestImpl(conversation, listOf(user2Username, user3Username))
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can add members to group chat`() {
        val conversation = runBlockingWithTestTimeout {
            conversationService.createGroupChat(
                courseId = course.id!!,
                groupMembers = listOf(user1Username, user2Username),
                authToken = accessToken,
                serverUrl = testServerUrl
            ).orThrow("Could not create group chat")
        }

        addRecipientsTestImpl(conversation, listOf(user3Username))
    }

    private fun addRecipientsTestImpl(conversation: Conversation, newUsers: List<String>) {
        val viewModel = ConversationAddMembersViewModel(
            courseId = course.id!!,
            conversationId = conversation.id,
            conversationService = get(),
            accountService = get(),
            serverConfigurationService = get(),
            networkStatusProvider = get(),
            savedStateHandle = SavedStateHandle(),
            coroutineContext = testDispatcher
        )

        var isDone = false

        composeTestRule.setContent {
            ConversationUserSelectionScreen(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
                displayFailedDialog = false,
                fabTestTag = TEST_TAG_ADD_MEMBERS_BUTTON,
                canSubmit = true,
                startJob = viewModel::addMembers,
                onJobCompleted = {
                    isDone = it == true
                },
                onNavigateBack = { isDone = true },
                onDismissFailedDialog = {}
            )
        }

        // Wait until screen is loaded
        composeTestRule.waitUntilAtLeastOneExists(
            hasTestTag(TEST_TAG_MEMBER_SELECTION_SEARCH_FIELD),
            DefaultTimeoutMillis
        )

        newUsers.forEach { username ->
            addMember(username, testDispatcher)
        }

        newUsers.forEach { username ->
            composeTestRule.onNodeWithTag(getTestTagForRecipient(username))
                .performScrollTo()
                .assertExists()
        }

        // Click on add button to add users.
        composeTestRule
            .onNodeWithTag(TEST_TAG_ADD_MEMBERS_BUTTON)
            .performClick()

        composeTestRule.waitUntil(DefaultTimeoutMillis) { isDone }

        val recipients = runBlockingWithTestTimeout {
            conversationService
                .getMembers(course.id!!, conversation.id, "", 10, 0, accessToken, testServerUrl)
                .orThrow("Could not load new recipients")
        }.map { it.username.orEmpty() }

        newUsers.forEach { username ->
            assertContains(recipients, username)
        }
    }

    private fun addMember(username: String, dispatcher: TestDispatcher) {
        composeTestRule
            .onNodeWithTag(TEST_TAG_MEMBER_SELECTION_SEARCH_FIELD)
            .performTextClearance()

        composeTestRule
            .onNodeWithTag(TEST_TAG_MEMBER_SELECTION_SEARCH_FIELD)
            .performTextInput(username)

        dispatcher.scheduler.advanceTimeBy(MemberSelectionBaseViewModel.QUERY_DEBOUNCE_TIME * 2)

        composeTestRule.waitUntilAtLeastOneExists(
            hasTestTag(testTagForPotentialRecipient(username)),
            DefaultTimeoutMillis
        )

        composeTestRule
            .onNode(
                hasAnyAncestor(hasTestTag(testTagForPotentialRecipient(username))) and hasContentDescription(
                    context.getString(R.string.conversation_member_selection_content_description_add_recipient)
                )
            )
            .performScrollTo()
            .performClick()
    }
}