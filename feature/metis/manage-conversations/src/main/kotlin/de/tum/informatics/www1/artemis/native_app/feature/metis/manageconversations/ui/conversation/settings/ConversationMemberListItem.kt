package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddModerator
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.RemoveModerator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ConversationUser
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.common.CourseUserListItem

internal fun getUserOptionsTestTag(username: String) = "userOptions$username"

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
    var showUserOptionsMenu by remember { mutableStateOf(false) }
    var iconButtonRef by remember { mutableStateOf<LayoutCoordinates?>(null) }

    CourseUserListItem(
        modifier = modifier,
        user = member,
        trailingContent = {
            Box {
                IconButton(
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(32.dp)
                        .testTag(getUserOptionsTestTag(member.username ?: ""))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .onGloballyPositioned { coordinates ->
                            iconButtonRef = coordinates
                        },
                    onClick = { showUserOptionsMenu = true }
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = Icons.Default.MoreHoriz,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = null
                    )
                }

                UserOptionsDropdownMenu(
                    conversation = conversation,
                    member = member,
                    isUserOptionsDropdownExpanded = showUserOptionsMenu,
                    onRequestKickMember = onRequestKickMember,
                    onRequestGrantModerationPermission = onRequestGrantModerationPermission,
                    onRequestRevokeModerationPermission = onRequestRevokeModerationPermission,
                    onDismiss = { showUserOptionsMenu = false }
                )
            }
        }
    )
}

@Composable
private fun UserOptionsDropdownMenu(
    conversation: Conversation,
    member: ConversationUser,
    isUserOptionsDropdownExpanded: Boolean,
    onRequestKickMember: (ConversationUser) -> Unit,
    onRequestGrantModerationPermission: (ConversationUser) -> Unit,
    onRequestRevokeModerationPermission: (ConversationUser) -> Unit,
    onDismiss: () -> Unit
) {
    val (moderatorIcon, moderatorText) = if (member.isChannelModerator) {
        Pair(Icons.Default.RemoveModerator, R.string.conversation_members_content_description_remove_moderator)
    } else {
        Pair(Icons.Default.AddModerator, R.string.conversation_members_content_description_add_moderator)
    }

    DropdownMenu(
        expanded = isUserOptionsDropdownExpanded,
        onDismissRequest = onDismiss,
        offset = DpOffset(0.dp, 4.dp)
    ) {
        DropdownMenuItem(
            modifier = Modifier.testTag(getUserOptionsTestTag(member.username ?: "") + "_kick"),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.PersonRemove,
                    contentDescription = stringResource(id = R.string.conversation_members_content_description_kick_user)
                )
            },
            text = {
                Text(text = stringResource(id = R.string.conversation_members_content_description_kick_user))
            },
            onClick = {
                onRequestKickMember(member)
                onDismiss()
            }
        )

        if (conversation is ChannelChat) {
            DropdownMenuItem(
                modifier = Modifier.testTag(getUserOptionsTestTag(member.username ?: "") + "_grant_moderator"),
                leadingIcon = {
                    Icon(
                        imageVector = moderatorIcon,
                        contentDescription = stringResource(id = moderatorText)
                    )
                },
                text = {
                    Text(text = stringResource(moderatorText))
                },
                onClick = {
                    if (member.isChannelModerator) {
                        onRequestRevokeModerationPermission(member)
                    } else {
                        onRequestGrantModerationPermission(member)
                    }
                    onDismiss()
                }
            )
        }
    }
}