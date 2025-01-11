package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddModerator
import androidx.compose.material.icons.filled.GroupRemove
import androidx.compose.material.icons.filled.RemoveModerator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ConversationUser
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.hasModerationRights
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.ConversationUserRoleBadgesRow
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.ProfilePictureWithDialog

@Composable
internal fun ConversationMemberListItem(
    modifier: Modifier,
    member: ConversationUser,
    clientUsername: String,
    conversation: Conversation,
    onRequestKickMember: (ConversationUser) -> Unit,
    onRequestGrantModerationPermission: (ConversationUser) -> Unit,
    onRequestRevokeModerationPermission: (ConversationUser) -> Unit
) {
    ListItem(
        modifier = modifier,
        headlineContent = {
            ConversationUserRoleBadgesRow(
                user = member
            )
        },
        supportingContent = member.name?.let {
            {
                Text(
                    text = it,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        leadingContent = {
            ProfilePictureWithDialog(
                conversationUser = member
            )
        },
        trailingContent = {
            if (member.username != clientUsername && conversation.hasModerationRights) {
                Row {
                    IconButton(onClick = { onRequestKickMember(member) }) {
                        Icon(
                            imageVector = Icons.Default.GroupRemove,
                            contentDescription = stringResource(id = R.string.conversation_members_content_description_kick_user)
                        )
                    }

                    if (conversation is ChannelChat) {
                        IconButton(
                            onClick = {
                                if (member.isChannelModerator) {
                                    onRequestRevokeModerationPermission(member)
                                } else {
                                    onRequestGrantModerationPermission(member)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (member.isChannelModerator) Icons.Default.RemoveModerator else Icons.Default.AddModerator,
                                contentDescription = stringResource(
                                    id = if (member.isChannelModerator) R.string.conversation_members_content_description_remove_moderator
                                    else R.string.conversation_members_content_description_add_moderator
                                )
                            )
                        }
                    }
                }
            }
        }
    )
}
