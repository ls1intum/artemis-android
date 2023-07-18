package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.ui.AwaitDeferredCompletion
import de.tum.informatics.www1.artemis.native_app.core.ui.alert.TextAlertDialog
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.conversation.GroupChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.conversation.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.humanReadableName
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.humanReadableTitle
import kotlinx.coroutines.Deferred

/**
 * Displays the dialogs for confirming the user wants to perform on a certain conversation user, including kicking, giving and revoking rights
 * Also performs the given action when clicked on the positive buttons of the dialog.
 */
@Composable
internal fun PerformActionOnUserDialogs(
    conversation: Conversation,
    performActionOnUserData: PerformActionOnUserData?,
    viewModel: SettingsBaseViewModel,
    onDismiss: () -> Unit
) {
    var performActionOnUserJob: Deferred<Boolean>? by remember { mutableStateOf(null) }

    var displayPerformActionOnUserFailedDialog by remember { mutableStateOf(false) }

    AwaitDeferredCompletion(
        job = performActionOnUserJob,
        onComplete = { isSuccessful ->
            if (!isSuccessful) {
                displayPerformActionOnUserFailedDialog = true
            }
        }
    )

    if (performActionOnUserData != null) {
        PerformActionOnUserDialogs(
            conversation = conversation,
            performActionOnUserData = performActionOnUserData,
            onKickUser = {
                performActionOnUserJob = viewModel.kickMember(performActionOnUserData.user.username.orEmpty())
            },
            onGiveModerationRights = {
                performActionOnUserJob = viewModel.grantModerationRights(performActionOnUserData.user)
            },
            onRevokeModerationRights = {
                performActionOnUserJob = viewModel.revokeModerationRights(performActionOnUserData.user)
            },
            onDismiss = onDismiss
        )
    }

    if (displayPerformActionOnUserFailedDialog) {
        TextAlertDialog(
            title = stringResource(id = R.string.conversation_settings_perform_action_on_user_failed_dialog_title),
            text = stringResource(id = R.string.conversation_settings_perform_action_on_user_failed_dialog_message),
            confirmButtonText = stringResource(id = R.string.conversation_settings_perform_action_on_user_failed_dialog_positive),
            dismissButtonText = null,
            onPressPositiveButton = { displayPerformActionOnUserFailedDialog = false },
            onDismissRequest = { displayPerformActionOnUserFailedDialog = false }
        )
    }
}

@Composable
private fun PerformActionOnUserDialogs(
    conversation: Conversation,
    performActionOnUserData: PerformActionOnUserData,
    onKickUser: () -> Unit,
    onGiveModerationRights: () -> Unit,
    onRevokeModerationRights: () -> Unit,
    onDismiss: () -> Unit
) {
    val channelName = (conversation as? ChannelChat)?.name.orEmpty()

    val humanReadableName = performActionOnUserData.user.humanReadableName
    val username = performActionOnUserData.user.username.orEmpty()

    when (performActionOnUserData.userAction) {
        UserAction.KICK -> {
            when (conversation) {
                is ChannelChat -> KickUserFromChannelDialog(
                    humanReadableName = humanReadableName,
                    username = username,
                    channelName = conversation.name,
                    onPressPositiveButton = onKickUser,
                    onDismissRequest = onDismiss
                )

                is GroupChat -> KickUserFromChannelDialog(
                    humanReadableName = humanReadableName,
                    username = username,
                    channelName = conversation.humanReadableTitle,
                    onPressPositiveButton = onKickUser,
                    onDismissRequest = onDismiss
                )

                is OneToOneChat -> {}
            }
        }

        UserAction.GIVE_MODERATION_RIGHTS -> {
            GiveModerationRightsDialog(
                humanReadableName = humanReadableName,
                username = username,
                channelName = channelName,
                onPressPositiveButton = onGiveModerationRights,
                onDismissRequest = onDismiss
            )
        }

        UserAction.REVOKE_MODERATION_RIGHTS -> {
            RevokeModerationRightsDialog(
                humanReadableName = humanReadableName,
                username = username,
                channelName = channelName,
                onPressPositiveButton = onRevokeModerationRights,
                onDismissRequest = onDismiss
            )
        }
    }
}
