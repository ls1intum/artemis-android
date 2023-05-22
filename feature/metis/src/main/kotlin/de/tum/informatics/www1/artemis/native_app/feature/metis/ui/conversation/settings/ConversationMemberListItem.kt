package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddModerator
import androidx.compose.material.icons.filled.GroupRemove
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RemoveModerator
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.ConversationUser
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.hasModerationRights
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.humanReadableName

@Composable
internal fun ConversationMemberListItem(
    member: ConversationUser,
    clientUsername: String,
    conversation: Conversation,
    onRequestKickMember: (ConversationUser) -> Unit,
    onRequestGrantModerationPermission: (ConversationUser) -> Unit,
    onRequestRevokeModerationPermission: (ConversationUser) -> Unit
) {
    ListItem(
        headlineText = {
            Text(text = member.humanReadableName)
        },
        supportingText = member.username?.let { username ->
            {
                Text(text = username)
            }
        },
        leadingContent = {
            Row {
                val personIcon = when {
                    member.isInstructor -> Icons.Default.School
                    member.isEditor || member.isTeachingAssistant -> Icons.Default.SupervisorAccount
                    else -> Icons.Default.Person
                }

                Icon(imageVector = personIcon, contentDescription = null)

                if (member.isChannelModerator) {
                    Icon(imageVector = Icons.Default.Shield, contentDescription = null)
                }
            }
        },
        trailingContent = {
            if (member.username != clientUsername && conversation.hasModerationRights) {
                Row {
                    IconButton(onClick = { onRequestKickMember(member) }) {
                        Icon(
                            imageVector = Icons.Default.GroupRemove,
                            contentDescription = null
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
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }
    )
}
