package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.create_personal_conversation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.common.ConversationUserSelectionScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

internal const val TEST_TAG_CREATE_PERSONAL_CONVERSATION_BUTTON = "create personal conversation button"

@Composable
fun CreatePersonalConversationScreen(
    modifier: Modifier,
    courseId: Long,
    onConversationCreated: (conversationId: Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    val viewModel = koinViewModel<CreatePersonalConversationViewModel> { parametersOf(courseId) }
    val canCreateConversation by viewModel.canCreateConversation.collectAsState()
    var displayFailedDialog by remember { mutableStateOf(false) }

    ConversationUserSelectionScreen(
        modifier = modifier,
        viewModel = viewModel,
        displayFailedDialog = displayFailedDialog,
        fabTestTag = TEST_TAG_CREATE_PERSONAL_CONVERSATION_BUTTON,
        canSubmit = canCreateConversation,
        startJob = viewModel::createConversation,
        onJobCompleted = { conversation ->
            if (conversation != null) {
                onConversationCreated(conversation.id)
            } else {
                displayFailedDialog = true
            }
        },
        onDismissFailedDialog = { displayFailedDialog = false },
        onNavigateBack = onNavigateBack
    )
}