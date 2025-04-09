package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.overview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import de.tum.informatics.www1.artemis.native_app.core.ui.common.ArtemisSection
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.ConversationMemberListItem
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ConversationUser
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.hasModerationRights

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
    val showOptionsRow = hasMoreMembers || (conversation !is OneToOneChat && conversation.hasModerationRights)

    // ListItem applies its own padding, therefore, we need to pad the other items ourselves
    ArtemisSection(
        modifier = modifier,
        title = stringResource(id = R.string.conversation_settings_section_members, memberCount, memberCount),
    ) {
        ConversationMemberPreviewList(
            modifier = Modifier.fillMaxWidth(),
            conversation = conversation,
            clientUsername = clientUsername,
            members = members,
            onRequestKickMember = onRequestKickMember,
            onRequestGrantModerationPermission = onRequestGiveModerationRights,
            onRequestRevokeModerationPermission = onRequestRevokeModerationRights
        )

        if (showOptionsRow) {
            HorizontalDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hasMoreMembers) {
                    TextButton(onClick = onRequestViewAllMembers) {
                        Text(text = stringResource(id = R.string.conversation_settings_section_members_view_all_members))
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                if (conversation !is OneToOneChat && conversation.hasModerationRights) {
                    Button(onClick = onRequestAddMembers) {
                        Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(id = R.string.conversation_settings_section_members_add_members))
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationMemberPreviewList(
    modifier: Modifier,
    conversation: Conversation,
    clientUsername: String,
    members: List<ConversationUser>,
    onRequestKickMember: (ConversationUser) -> Unit,
    onRequestGrantModerationPermission: (ConversationUser) -> Unit,
    onRequestRevokeModerationPermission: (ConversationUser) -> Unit
) {
    Column(modifier = modifier) {
        members.fastForEachIndexed { index, member ->
            ConversationMemberListItem(
                modifier = Modifier,
                member = member,
                clientUsername = clientUsername,
                conversation = conversation,
                onRequestKickMember = onRequestKickMember,
                onRequestGrantModerationPermission = onRequestGrantModerationPermission,
                onRequestRevokeModerationPermission = onRequestRevokeModerationPermission
            )

            if (index < members.lastIndex) HorizontalDivider()
        }
    }
}
