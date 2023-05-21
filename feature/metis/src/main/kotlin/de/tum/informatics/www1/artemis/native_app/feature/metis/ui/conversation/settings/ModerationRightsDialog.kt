package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.ui.alert.MarkdownTextAlertDialog
import de.tum.informatics.www1.artemis.native_app.feature.metis.R

@Composable
internal fun GiveModerationRightsDialog(
    humanReadableName: String,
    username: String,
    channelName: String,
    onPressPositiveButton: () -> Unit,
    onDismissRequest: () -> Unit
) {
    MarkdownTextAlertDialog(
        title = stringResource(id = R.string.conversation_settings_dialog_grant_moderation_rights_title),
        text = stringResource(
            id = R.string.conversation_settings_dialog_grant_moderation_rights_message,
            humanReadableName,
            username,
            channelName
        ),
        confirmButtonText = stringResource(id = R.string.conversation_settings_dialog_grant_moderation_rights_positive),
        dismissButtonText = stringResource(id = R.string.conversation_settings_dialog_grant_moderation_rights_negative),
        onPressPositiveButton = onPressPositiveButton,
        onDismissRequest = onDismissRequest
    )
}

@Composable
internal fun RevokeModerationRightsDialog(
    humanReadableName: String,
    username: String,
    channelName: String,
    onPressPositiveButton: () -> Unit,
    onDismissRequest: () -> Unit
) {
    MarkdownTextAlertDialog(
        title = stringResource(id = R.string.conversation_settings_dialog_revoke_moderation_rights_title),
        text = stringResource(
            id = R.string.conversation_settings_dialog_revoke_moderation_rights_message,
            humanReadableName,
            username,
            channelName
        ),
        confirmButtonText = stringResource(id = R.string.conversation_settings_dialog_revoke_moderation_rights_positive),
        dismissButtonText = stringResource(id = R.string.conversation_settings_dialog_revoke_moderation_rights_negative),
        onPressPositiveButton = onPressPositiveButton,
        onDismissRequest = onDismissRequest
    )
}
