package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.overview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.alert.MarkdownTextAlertDialog
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.hasModerationRights

@Composable
internal fun ConversationOtherSettings(
    modifier: Modifier,
    conversation: Conversation,
    onLeaveConversation: () -> Unit,
    onToggleChannelArchivation: () -> Unit,
    onDeleteChannel: () -> Unit
) {
    var displayArchiveChannelDialog by remember { mutableStateOf(false) }
    var displayDeleteChannelDialog by remember { mutableStateOf(false) }

    val buttonModifier = Modifier.fillMaxWidth()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(id = R.string.conversation_settings_section_other),
            style = ConversationSettingsSectionTextStyle
        )

        if (conversation !is ChannelChat || !conversation.isCreator) {
            OutlinedButton(
                modifier = buttonModifier,
                onClick = onLeaveConversation
            ) {
                Text(text = stringResource(id = R.string.conversation_settings_section_other_leave_conversation))
            }
        }

        if (conversation.hasModerationRights && conversation is ChannelChat) {
            OutlinedButton(
                modifier = buttonModifier,
                onClick = { displayArchiveChannelDialog = true }
            ) {
                Text(
                    text = stringResource(
                        id = if (conversation.isArchived) {
                            R.string.conversation_settings_section_other_unarchive_channel
                        } else {
                            R.string.conversation_settings_section_other_archive_channel
                        }
                    )
                )
            }

            OutlinedButton(
                modifier = buttonModifier,
                onClick = { displayDeleteChannelDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
                border = BorderStroke(1.dp, SolidColor(MaterialTheme.colorScheme.error))
            ) {
                Text(
                    text = stringResource(R.string.conversation_settings_section_delete_channel)
                )
            }
        }
    }

    // Archive/Unarchive Confirmation Dialog
    if (displayArchiveChannelDialog) {
        val channelName = when (conversation) {
            is ChannelChat -> conversation.name
            else -> ""
        }

        val doArchive = when (conversation) {
            is ChannelChat -> !conversation.isArchived
            else -> false
        }

        val title =
            if (doArchive) R.string.conversation_settings_dialog_archive_channel_title else R.string.conversation_settings_dialog_unarchive_channel_title

        val text =
            if (doArchive) R.string.conversation_settings_dialog_archive_channel_message else R.string.conversation_settings_dialog_unarchive_channel_message

        val confirm =
            if (doArchive) R.string.conversation_settings_dialog_archive_channel_positive else R.string.conversation_settings_dialog_unarchive_channel_positive

        val dismiss =
            if (doArchive) R.string.conversation_settings_dialog_archive_channel_negative else R.string.conversation_settings_dialog_unarchive_channel_negative

        MarkdownTextAlertDialog(
            title = stringResource(id = title),
            text = stringResource(
                id = text,
                channelName
            ),
            confirmButtonText = stringResource(id = confirm),
            dismissButtonText = stringResource(id = dismiss),
            onPressPositiveButton = onToggleChannelArchivation,
            onDismissRequest = { displayArchiveChannelDialog = false })
    }

    // Delete Channel Confirmation Dialog
    if (displayDeleteChannelDialog) {
        val channelName = when (conversation) {
            is ChannelChat -> conversation.name
            else -> ""
        }

        MarkdownTextAlertDialog(
            title = stringResource(R.string.conversation_settings_section_delete_channel_title),
            text = stringResource(
                R.string.conversation_settings_section_delete_channel_message,
                channelName
            ),
            confirmButtonText = stringResource(R.string.conversation_settings_section_delete_channel),
            dismissButtonText = stringResource(R.string.conversation_settings_section_delete_channel_negative),
            onPressPositiveButton = {
                displayDeleteChannelDialog = false
                onDeleteChannel()
            },
            onDismissRequest = { displayDeleteChannelDialog = false }
        )
    }
}
