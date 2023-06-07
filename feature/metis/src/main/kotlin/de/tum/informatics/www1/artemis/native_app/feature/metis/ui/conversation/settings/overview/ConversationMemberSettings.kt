package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.ConversationUser
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings.ConversationMemberListItem

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

            if (conversation !is OneToOneChat) {
                Button(onClick = onRequestAddMembers) {
                    Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null)

                    Text(text = stringResource(id = R.string.conversation_settings_section_members_add_members))
                }
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
            ConversationMemberListItem(
                member = member,
                clientUsername = clientUsername,
                conversation = conversation,
                onRequestKickMember = onRequestKickMember,
                onRequestGrantModerationPermission = onRequestGrantModerationPermission,
                onRequestRevokeModerationPermission = onRequestRevokeModerationPermission
            )
        }

        if (hasMoreMembers) {
            TextButton(onClick = onRequestViewAllMembers) {
                Text(text = stringResource(id = R.string.conversation_settings_section_members_view_all_members))
            }
        }
    }
}
