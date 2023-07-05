package de.tum.informatics.www1.artemis.native_app.feature.metis.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.feature.metis.ConversationBaseTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings.overview.ConversationSettingsScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings.overview.ConversationSettingsViewModel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.koin.test.get

@OptIn(ExperimentalTestApi::class)
abstract class ConversationSettingsBaseE2eTest : ConversationBaseTest() {

    protected fun changeConversationDetailsTestImpl(
        conversation: Conversation,

        performChanges: () -> Unit
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
        onConversationLeft: () -> Unit = {}
    ) {
        val viewModel = ConversationSettingsViewModel(
            initialCourseId = course.id!!,
            initialConversationId = conversation.id,
            conversationService = get(),
            accountService = get(),
            serverConfigurationService = get(),
            networkStatusProvider = get(),
            accountDataService = get(),
            savedStateHandle = SavedStateHandle(),
            coroutineContext = UnconfinedTestDispatcher()
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
                onConversationLeft = onConversationLeft
            )
        }
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

    private fun awaitConversationLoaded() {
        composeTestRule.waitUntilAtLeastOneExists(
            hasText(context.getString(R.string.conversation_settings_section_more_info)),
            DefaultTimeoutMillis
        )
    }
}