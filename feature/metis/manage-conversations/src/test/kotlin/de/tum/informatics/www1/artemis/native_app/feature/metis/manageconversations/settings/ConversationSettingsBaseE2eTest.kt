package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import de.tum.informatics.www1.artemis.native_app.core.common.test.testServerUrl
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.overview.ConversationSettingsScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.overview.ConversationSettingsViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.getConversation
import de.tum.informatics.www1.artemis.native_app.feature.metistest.ConversationBaseTest
import org.koin.test.get
import kotlin.test.assertIs

@OptIn(ExperimentalTestApi::class)
internal abstract class ConversationSettingsBaseE2eTest : ConversationBaseTest() {

    protected inline fun <reified T : Conversation> changeConversationDetailsTestImpl(
        conversation: T,
        performChanges: () -> Unit,
        verifyChanges: (T) -> Unit
    ) {
        setupUiAndViewModel(conversation)

        // Wait for content to be loaded
        awaitConversationLoaded()

        performChanges()

        composeTestRule
            .onNodeWithText(context.getString(R.string.conversation_settings_basic_data_save))
            .performClick()

        // text was saved successfully when the button disappeared.
        composeTestRule
            .waitUntilDoesNotExist(hasText(context.getString(R.string.conversation_settings_basic_data_save)))

        val updatedConversation = runBlockingWithTestTimeout {
            conversationService
                .getConversation(
                    courseId = course.id!!,
                    conversationId = conversation.id,
                    authToken = accessToken,
                    serverUrl = testServerUrl
                )
                .orThrow("Could not load updated conversation")
        }

        verifyChanges(assertIs(updatedConversation, "Loaded conversation is not of correct type"))
    }

    protected fun changeTitleText(currentTitle: String?, newTitle: String) {
        changeConversationDetailsText(
            currentTitle ?: context.getString(R.string.conversation_settings_basic_data_name),
            context.getString(R.string.conversation_settings_basic_data_name_empty),
            newTitle
        )
    }

    protected fun changeDescriptionText(currentDescription: String?, newDescription: String) {
        changeConversationDetailsText(
            currentDescription
                ?: context.getString(R.string.conversation_settings_basic_data_description),
            context.getString(R.string.conversation_settings_basic_data_description_empty),
            newDescription
        )
    }

    protected fun changeConversationDetailsText(
        initialText: String,
        emptyText: String,
        newText: String
    ) {
        composeTestRule
            .onNode(hasSetTextAction() and hasText(initialText))
            .performScrollTo()
            .performTextClearance()

        composeTestRule
            .onNode(hasSetTextAction() and hasText(emptyText))
            .performScrollTo()
            .performTextInput(newText)
    }

    protected fun setupUiAndViewModel(
        conversation: Conversation,
        onConversationLeft: () -> Unit = {},
        onChannelDeleted: () -> Unit = {}
    ): ConversationSettingsViewModel {
        val viewModel = ConversationSettingsViewModel(
            initialCourseId = course.id!!,
            initialConversationId = conversation.id,
            conversationService = get(),
            accountService = get(),
            serverConfigurationService = get(),
            networkStatusProvider = get(),
            accountDataService = get(),
            savedStateHandle = SavedStateHandle(),
            coroutineContext = testDispatcher
        )

        composeTestRule.setContent {
            ConversationSettingsScreen(
                modifier = Modifier.fillMaxSize(),
                courseId = course.id!!,
                conversationId = conversation.id,
                viewModel = viewModel,
                onNavigateBack = { },
                onRequestAddMembers = { },
                onRequestViewAllMembers = { },
                onConversationLeft = onConversationLeft,
                onChannelDeleted = onChannelDeleted,
                onSidebarToggle = { },
            )
        }

        return viewModel
    }

    protected fun canLeaveConversationTestImpl(conversation: Conversation) {
        var hasLeftConversation = false

        setupUiAndViewModel(conversation, onConversationLeft = {
            hasLeftConversation = true
        })

        awaitConversationLoaded()

        composeTestRule
            .onNodeWithText(context.getString(R.string.conversation_settings_section_other_leave_conversation))
            .performScrollTo()
            .performClick()

        composeTestRule.waitUntil(DefaultTimeoutMillis) {
            hasLeftConversation
        }
    }

    protected fun awaitConversationLoaded() {
        composeTestRule.waitUntilAtLeastOneExists(
            hasText(context.getString(R.string.conversation_settings_section_more_info)),
            DefaultTimeoutMillis
        )
    }

    protected fun deleteChannelTestImpl() {
        val deleteButtonText =
            context.getString(R.string.conversation_settings_section_delete_channel)

        composeTestRule.waitUntilAtLeastOneExists(
            hasText(deleteButtonText),
            DefaultTimeoutMillis
        )

        composeTestRule
            .onNodeWithText(deleteButtonText)
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()

        composeTestRule
            .onNode(
                hasAnyAncestor(isDialog()) and hasText(context.getString(R.string.conversation_settings_section_delete_channel_title))
            )
            .assertExists()

        composeTestRule
            .onNode(
                hasAnyAncestor(isDialog()) and hasText(context.getString(R.string.conversation_settings_section_delete_channel))
            )
            .assertExists()
            .performClick()
    }

    protected fun toggleChannelPrivacyTestImpl(conversation: Conversation) {
        val isPublic = (conversation as ChannelChat).isPublic
        val toggleButtonText = context.getString(
            if (isPublic)
                R.string.conversation_settings_section_channel_toggle_privacy_private
            else
                R.string.conversation_settings_section_channel_toggle_privacy_public
        )

        val expectedTitle = context.getString(
            if (isPublic)
                R.string.conversation_settings_section_channel_toggle_privacy_title_private
            else
                R.string.conversation_settings_section_channel_toggle_privacy_title_public
        )

        val expectedButton = context.getString(
            if (isPublic)
                R.string.conversation_settings_section_channel_toggle_privacy_private_button
            else
                R.string.conversation_settings_section_channel_toggle_privacy_public_button
        )

        composeTestRule.waitUntilAtLeastOneExists(
            hasText(toggleButtonText),
            DefaultTimeoutMillis
        )

        composeTestRule
            .onNodeWithText(toggleButtonText)
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()

        composeTestRule
            .onNode(
                hasAnyAncestor(isDialog()) and hasText(expectedTitle)
            )
            .assertExists()

        composeTestRule
            .onNode(
                hasAnyAncestor(isDialog()) and hasText(expectedButton)
            )
            .assertExists()
            .performClick()

        composeTestRule
            .waitUntilExactlyOneExists(
                hasText(toggleButtonText),
                DefaultTimeoutMillis
            )
    }

}