package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.add_members

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.common.ConversationUserSelectionScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

internal const val TEST_TAG_ADD_MEMBERS_BUTTON = "add members to conversation button"

@Composable
fun ConversationAddMembersScreen(
    modifier: Modifier,
    courseId: Long,
    conversationId: Long,
    onNavigateBack: () -> Unit,
    onSidebarToggle: () -> Unit
) {
    val viewModel = koinViewModel<ConversationAddMembersViewModel> { parametersOf(courseId, conversationId) }
    val canAdd by viewModel.canAdd.collectAsState()
    var displayFailedDialog by remember { mutableStateOf(false) }

    ConversationUserSelectionScreen(
        modifier = modifier,
        viewModel = viewModel,
        fabTestTag = TEST_TAG_ADD_MEMBERS_BUTTON,
        fabIcon = Icons.Default.Done,
        titleRes = R.string.conversation_add_members_title,
        dialogTitleRes = R.string.create_personal_conversation_failed_title,
        dialogMessageRes = R.string.create_personal_conversation_failed_message,
        dialogConfirmTextRes = R.string.create_personal_conversation_failed_positive,
        canSubmit = canAdd,
        startJob = viewModel::addMembers,
        onJobCompleted = { success ->
            if (success == true) {
                onNavigateBack()
            } else {
                displayFailedDialog = true
            }
        },
        onNavigateBack = onNavigateBack,
        displayFailedDialog = displayFailedDialog,
        onDismissFailedDialog = { displayFailedDialog = false },
        onSidebarToggle = onSidebarToggle,
    )
}