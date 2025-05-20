package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.overview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.common.test.testServerUrl
import de.tum.informatics.www1.artemis.native_app.core.data.filterSuccess
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.feature.login.test.user2DisplayName
import de.tum.informatics.www1.artemis.native_app.feature.login.test.user2Username
import de.tum.informatics.www1.artemis.native_app.feature.login.test.user3Username
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.common.ConversationUserSelectionScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.create_personal_conversation.CreatePersonalConversationViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.create_personal_conversation.TEST_TAG_CREATE_PERSONAL_CONVERSATION_BUTTON
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.CourseUser
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.GroupChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.getConversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.member_selection.MemberSelectionBaseViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.member_selection.TEST_TAG_MEMBER_SELECTION_SEARCH_FIELD
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.member_selection.util.toMemberSelectionItem
import de.tum.informatics.www1.artemis.native_app.feature.metistest.ConversationBaseTest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.test.get
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertIs

@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
class CreateConversationE2eTest : ConversationBaseTest() {

    /**
     * Tests that upon entering the name of user2 their name will be displayed in the potential recipients list.
     */
    @OptIn(ExperimentalTestApi::class)
    @Test(timeout = DefaultTestTimeoutMillis)
    fun `displays users on search`() {
        val viewModel = setupUiAndViewModel()

        composeTestRule
            .onNodeWithTag(TEST_TAG_MEMBER_SELECTION_SEARCH_FIELD)
            .performTextInput(user2DisplayName)

        testDispatcher.scheduler.advanceTimeBy(MemberSelectionBaseViewModel.QUERY_DEBOUNCE_TIME * 2)

        runBlockingWithTestTimeout {
            viewModel.potentialRecipients.filterSuccess().filter { it.isNotEmpty() }.first()
        }

        composeTestRule.waitUntilAtLeastOneExists(
            hasText(user2DisplayName, substring = true) and !hasAnyAncestor(
                hasTestTag(
                    TEST_TAG_MEMBER_SELECTION_SEARCH_FIELD
                )
            ),
            DefaultTimeoutMillis
        )
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can create personal conversation`() {
        val createdConversation = createConversationTestImpl(
            listOf(CourseUser(username = user2Username))
        )

        assertIs<OneToOneChat>(createdConversation, "Created conversation is not a OneToOneChat")
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can create group conversation`() {
        val createdConversation = createConversationTestImpl(
            listOf(
                CourseUser(username = user2Username),
                CourseUser(username = user3Username)
            )
        )

        assertIs<GroupChat>(createdConversation, "Created conversation is not a group conversation")
    }

    private fun createConversationTestImpl(recipients: List<CourseUser>): Conversation {
        var createdConversationId: Long? = null

        val viewModel = setupUiAndViewModel(
            onConversationCreated = { conversationId ->
                createdConversationId = conversationId
            }
        )

        val memberItems = recipients.map { it.toMemberSelectionItem() }
        memberItems.forEach(viewModel::addMemberItem)

        composeTestRule
            .onNodeWithTag(TEST_TAG_CREATE_PERSONAL_CONVERSATION_BUTTON)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitUntil(DefaultTimeoutMillis) { createdConversationId != null }

        return runBlockingWithTestTimeout {
            conversationService.getConversation(
                course.id!!,
                createdConversationId!!,
                accessToken,
                testServerUrl
            )
                .orThrow("Could not load created conversation")
        }
    }

    private fun setupUiAndViewModel(
        onConversationCreated: (Long) -> Unit = {}
    ): CreatePersonalConversationViewModel {
        val viewModel = CreatePersonalConversationViewModel(
            courseId = course.id!!,
            conversationService = get(),
            accountService = get(),
            serverConfigurationService = get(),
            networkStatusProvider = get(),
            savedStateHandle = SavedStateHandle(),
            coroutineContext = testDispatcher
        )

        composeTestRule.setContent {
            ConversationUserSelectionScreen(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
                displayFailedDialog = false,
                fabTestTag = TEST_TAG_CREATE_PERSONAL_CONVERSATION_BUTTON,
                canSubmit = true,
                startJob = viewModel::createConversation,
                onJobCompleted = { conversation ->
                    if (conversation != null) {
                        onConversationCreated(conversation.id)
                    }
                },
                onNavigateBack = {},
                onDismissFailedDialog = {},
                onSidebarToggle = {}
            )
        }

        return viewModel
    }
}