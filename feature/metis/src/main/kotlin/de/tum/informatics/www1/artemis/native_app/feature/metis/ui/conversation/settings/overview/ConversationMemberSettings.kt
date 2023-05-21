package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddModerator
import androidx.compose.material.icons.filled.GroupRemove
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.RemoveModerator
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.ConversationUser
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.hasModerationRights
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.humanReadableName

@Composable
internal fun ConversationMemberSettings(
    modifier: Modifier,
    conversation: Conversation,
    clientUsername: String,
    memberCount: Int,
    members: List<ConversationUser>,
    hasMoreMembers: Boolean,
    onRequestAddMembers: () -> Unit,
    onRequestViewAllMembers: () -> Unit,
    onRequestKickMember: (ConversationUser) -> Unit,
    onRequestGiveModerationRights: (ConversationUser) -> Unit,
    onRequestRevokeModerationRights: (ConversationUser) -> Unit
) {
    // ListItem applies its own padding, therefore, we need to pad the other items ourselves

    val columnItemModifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = Spacings.ScreenHorizontalSpacing)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            modifier = columnItemModifier,
            text = stringResource(id = R.string.conversation_settings_section_members),
            style = ConversationSettingsSectionTextStyle
        )

        Row(
            modifier = columnItemModifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = pluralStringResource(
                    id = R.plurals.conversation_settings_section_members_count,
                    count = memberCount,
                    memberCount
                )
            )

            Button(onClick = onRequestAddMembers) {
                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null)

                Text(text = stringResource(id = R.string.conversation_settings_section_members_add_members))
            }
        }


        ConversationMemberPreviewList(
            modifier = Modifier.fillMaxWidth(),
            conversation = conversation,
            clientUsername = clientUsername,
            members = members,
            hasMoreMembers = hasMoreMembers,
            onRequestViewAllMembers = onRequestViewAllMembers,
            onRequestKickMember = onRequestKickMember,
            onRequestGrantModerationPermission = onRequestGiveModerationRights,
            onRequestRevokeModerationPermission = onRequestRevokeModerationRights
        )
    }
}

@Composable
private fun ConversationMemberPreviewList(
    modifier: Modifier,
    conversation: Conversation,
    clientUsername: String,
    members: List<ConversationUser>,
    hasMoreMembers: Boolean,
    onRequestViewAllMembers: () -> Unit,
    onRequestKickMember: (ConversationUser) -> Unit,
    onRequestGrantModerationPermission: (ConversationUser) -> Unit,
    onRequestRevokeModerationPermission: (ConversationUser) -> Unit
) {
    Column(modifier = modifier) {
        members.fastForEach { member ->
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
                                            onRequestGrantModerationPermission(member)
                                        } else {
                                            onRequestRevokeModerationPermission(member)
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

        if (hasMoreMembers) {
            TextButton(onClick = onRequestViewAllMembers) {
                Text(text = stringResource(id = R.string.conversation_settings_section_members_view_all_members))
            }
        }
    }
}
