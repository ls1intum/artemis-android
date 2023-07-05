package de.tum.informatics.www1.artemis.native_app.feature.metis.members

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.printToString
import androidx.lifecycle.SavedStateHandle
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.testServerUrl
import de.tum.informatics.www1.artemis.native_app.feature.login.test.testUsername
import de.tum.informatics.www1.artemis.native_app.feature.metis.ConversationBaseTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.create_personal_conversation.testTagForPotentialRecipient
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.member_selection.TEST_TAG_MEMBER_SELECTION_SEARCH_FIELD
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.member_selection.testTagForSelectedRecipient
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings.add_members.ConversationAddMembersScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings.add_members.ConversationAddMembersViewModel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.withTimeout
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

    @Test
    fun `can add members to channel`() {
        val conversation = runBlocking {
            withTimeout(DefaultTimeoutMillis) {
                conversationService.createChannel(
                    courseId = course.id!!,
                    name = "testchannel",
                    description = "",
                    isPublic = true,
                    isAnnouncement = true,
                    authToken = accessToken,
                    serverUrl = testServerUrl
                ).orThrow("Could not create channel")
            }
        }

        addRecipientsTestImpl(conversation, listOf(user2Username, user3Username))
    }

    @Test
    fun `can add members to group chat`() {
        val conversation = runBlocking {
            withTimeout(DefaultTimeoutMillis) {
                conversationService.createGroupChat(
                    courseId = course.id!!,
                    groupMembers = listOf(testUsername, user2Username),
                    authToken = accessToken,
                    serverUrl = testServerUrl
                ).orThrow("Could not create group chat")
            }
        }

        addRecipientsTestImpl(conversation, listOf(user3Username))
    }

    private fun addRecipientsTestImpl(conversation: Conversation, newUsers: List<String>) {
        val dispatcher = UnconfinedTestDispatcher()

        val viewModel = ConversationAddMembersViewModel(
            courseId = course.id!!,
            conversationId = conversation.id,
            conversationService = get(),
            accountService = get(),
            serverConfigurationService = get(),
            networkStatusProvider = get(),
            savedStateHandle = SavedStateHandle(),
            coroutineContext = dispatcher
        )

        var isDone = false

        composeTestRule.setContent {
            ConversationAddMembersScreen(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
                onNavigateBack = { isDone = true }
            )
        }

        // Wait until screen is loaded
        composeTestRule.waitUntilAtLeastOneExists(
            hasTestTag(TEST_TAG_MEMBER_SELECTION_SEARCH_FIELD),
            DefaultTimeoutMillis
        )

        newUsers.forEach { username ->
            addMember(username, dispatcher)
        }

        newUsers.forEach { username ->
            composeTestRule.onNodeWithTag(testTagForSelectedRecipient(username)).assertExists()
        }

        // Click on add button to add users.
        composeTestRule
            .onNodeWithText(context.getString(R.string.conversation_add_members_button_add_members))
            .performClick()

        composeTestRule.waitUntil(DefaultTimeoutMillis) { isDone }

        val recipients = runBlocking {
            withTimeout(DefaultTimeoutMillis) {
                conversationService
                    .getMembers(course.id!!, conversation.id, "", 10, 0, accessToken, testServerUrl)
                    .orThrow("Could not load new recipients")
            }.map { it.username.orEmpty() }
        }

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

        dispatcher.scheduler.advanceUntilIdle()
        dispatcher.scheduler.advanceTimeBy(20000)

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