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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ConversationUser
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.hasModerationRights
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.humanReadableName

@Composable
internal fun ConversationMemberListItem(
    modifier: Modifier,
    member: de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ConversationUser,
    clientUsername: String,
    conversation: de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation,
    onRequestKickMember: (de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ConversationUser) -> Unit,
    onRequestGrantModerationPermission: (de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ConversationUser) -> Unit,
    onRequestRevokeModerationPermission: (de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ConversationUser) -> Unit
) {
    ListItem(
        modifier = modifier,
        headlineContent = {
            Text(text = member.humanReadableName)
        },
        supportingContent = member.username?.let { username ->
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

                val contentDescription = when {
                    member.isInstructor -> R.string.conversation_members_content_description_instructor
                    member.isEditor -> R.string.conversation_members_content_description_editor
                    member.isTeachingAssistant -> R.string.conversation_members_content_description_teaching_assistant
                    else -> R.string.conversation_members_content_description_student
                }

                Icon(
                    imageVector = personIcon,
                    contentDescription = stringResource(id = contentDescription)
                )

                if (member.isChannelModerator) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = stringResource(id = R.string.conversation_members_content_description_moderator)
                    )
                }
            }
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

                    if (conversation is de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat) {
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
