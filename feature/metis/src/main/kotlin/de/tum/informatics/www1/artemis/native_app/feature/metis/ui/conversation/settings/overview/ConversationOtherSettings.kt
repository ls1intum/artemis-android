package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.alert.MarkdownTextAlertDialog
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.hasModerationRights

@Composable
internal fun ConversationOtherSettings(
    modifier: Modifier,
    conversation: de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation,
    onLeaveConversation: () -> Unit,
    onToggleChannelArchivation: () -> Unit
) {
    var displayArchiveChannelDialog by remember { mutableStateOf(false) }

    val buttonModifier = Modifier.fillMaxWidth()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(id = R.string.conversation_settings_section_other),
            style = ConversationSettingsSectionTextStyle
        )

        if (conversation !is de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat || !conversation.isCreator) {
            OutlinedButton(
                modifier = buttonModifier,
                onClick = onLeaveConversation
            ) {
                Text(text = stringResource(id = R.string.conversation_settings_section_other_leave_conversation))
            }
        }

        if (conversation.hasModerationRights && conversation is de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat) {
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
        }
    }

    if (displayArchiveChannelDialog) {
        val channelName = when (conversation) {
            is de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat -> conversation.name
            else -> ""
        }

        val doArchive = when (conversation) {
            is de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat -> !conversation.isArchived
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
}