package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.ui.alert.MarkdownTextAlertDialog
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.R

@Composable
internal fun KickUserFromChannelDialog(
    humanReadableName: String,
    username: String,
    channelName: String,
    onPressPositiveButton: () -> Unit,
    onDismissRequest: () -> Unit
) {
    MarkdownTextAlertDialog(
        title = stringResource(id = R.string.conversation_settings_dialog_kick_member_from_channel_title),
        text = stringResource(
            id = R.string.conversation_settings_dialog_kick_member_from_channel_message,
            humanReadableName,
            username,
            channelName
        ),
        confirmButtonText = stringResource(id = R.string.conversation_settings_dialog_kick_member_positive),
        dismissButtonText = stringResource(id = R.string.conversation_settings_dialog_kick_member_negative),
        onPressPositiveButton = onPressPositiveButton,
        onDismissRequest = onDismissRequest
    )
}

@Composable
internal fun KickUserFromGroupDialog(
    humanReadableName: String,
    username: String,
    groupName: String,
    onPressPositiveButton: () -> Unit,
    onDismissRequest: () -> Unit
) {
    MarkdownTextAlertDialog(
        title = stringResource(id = R.string.conversation_settings_dialog_kick_member_from_group_title,),
        text = stringResource(
            id = R.string.conversation_settings_dialog_kick_member_from_group_message,
            humanReadableName,
            username
        ),
        confirmButtonText = stringResource(id = R.string.conversation_settings_dialog_kick_member_positive),
        dismissButtonText = stringResource(id = R.string.conversation_settings_dialog_kick_member_negative),
        onPressPositiveButton = onPressPositiveButton,
        onDismissRequest = onDismissRequest
    )
}