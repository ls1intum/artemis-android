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

        if (conversation is ChannelChat) {
            val isCreator = conversation.isCreator
            val hasChannelModerationRights = conversation.hasChannelModerationRights
            val isChannelModerator = conversation.isChannelModerator
            val isTutorialGroupChannel = conversation.tutorialGroupId != null || conversation.tutorialGroupTitle != null

            val canManageChannels = !isTutorialGroupChannel && hasChannelModerationRights && isChannelModerator && isCreator

            // Archive/Unarchive and Delete Buttons
            if (canManageChannels) {
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
    }

    if (displayArchiveChannelDialog && conversation is ChannelChat) {
        ArchiveChannelDialog(
            conversation = conversation,
            onToggleChannelArchivation = {
                displayArchiveChannelDialog = false
                onToggleChannelArchivation()
            },
            onDismiss = { displayArchiveChannelDialog = false }
        )
    }

    if (displayDeleteChannelDialog && conversation is ChannelChat) {
        DeleteChannelDialog(
            conversation = conversation,
            onDeleteChannel = onDeleteChannel,
            onDismiss = { displayDeleteChannelDialog = false }
        )
    }
}

@Composable
private fun DeleteChannelDialog(
    conversation: ChannelChat,
    onDeleteChannel: () -> Unit,
    onDismiss: () -> Unit
) {
    MarkdownTextAlertDialog(
        title = stringResource(R.string.conversation_settings_section_delete_channel_title),
        text = stringResource(
            R.string.conversation_settings_section_delete_channel_message,
            conversation.name
        ),
        confirmButtonText = stringResource(R.string.conversation_settings_section_delete_channel),
        dismissButtonText = stringResource(R.string.conversation_settings_section_delete_channel_negative),
        onPressPositiveButton = {
            onDismiss()
            onDeleteChannel()
        },
        onDismissRequest = onDismiss
    )
}

@Composable
private fun ArchiveChannelDialog(
    conversation: ChannelChat,
    onToggleChannelArchivation: () -> Unit,
    onDismiss: () -> Unit
) {
    val doArchive = !conversation.isArchived
    val channelName = conversation.name

    val titleRes = if (doArchive)
        R.string.conversation_settings_dialog_archive_channel_title
    else
        R.string.conversation_settings_dialog_unarchive_channel_title

    val messageRes = if (doArchive)
        R.string.conversation_settings_dialog_archive_channel_message
    else
        R.string.conversation_settings_dialog_unarchive_channel_message

    val confirmRes = if (doArchive)
        R.string.conversation_settings_dialog_archive_channel_positive
    else
        R.string.conversation_settings_dialog_unarchive_channel_positive

    val dismissRes = if (doArchive)
        R.string.conversation_settings_dialog_archive_channel_negative
    else
        R.string.conversation_settings_dialog_unarchive_channel_negative

    MarkdownTextAlertDialog(
        title = stringResource(id = titleRes),
        text = stringResource(id = messageRes, channelName),
        confirmButtonText = stringResource(id = confirmRes),
        dismissButtonText = stringResource(id = dismissRes),
        onPressPositiveButton = onToggleChannelArchivation,
        onDismissRequest = onDismiss
    )
}