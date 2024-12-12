package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Groups2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.GroupChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.ProfilePicture
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.ProfilePictureData

@Composable
fun ConversationIcon(
    modifier: Modifier = Modifier,
    conversation: Conversation,
    clientId: Long,
) {
    when(conversation) {
        is ChannelChat -> ChannelChatIcon(modifier, conversation)
        is GroupChat -> GroupChatIcon(modifier)
        is OneToOneChat -> OneToOneChatIcon(modifier, conversation, clientId)
    }
}


@Composable
fun ChannelChatIcon(
    modifier: Modifier = Modifier,
    channelChat: ChannelChat
) {
    Icon(
        modifier = modifier,
        imageVector = getChannelIconImageVector(channelChat),
        contentDescription = null
    )
}

private fun getChannelIconImageVector(channelChat: ChannelChat): ImageVector {
    if (channelChat.isArchived) {
        return Icons.Default.Archive
    }
    if (channelChat.isAnnouncementChannel) {
        return Icons.Default.Campaign
    }
    if (channelChat.isPublic) {
        return Icons.Default.Numbers
    }
    return Icons.Default.Lock
}

@Composable
fun GroupChatIcon(modifier: Modifier) {
    Icon(
        modifier = modifier,
        imageVector = Icons.Default.Groups2,
        contentDescription = null
    )
}

@Composable
fun OneToOneChatIcon(
    modifier: Modifier,
    oneToOneChat: OneToOneChat,
    clientId: Long
) {
    val conversationPartner = oneToOneChat.members.first { it.id != clientId }
    ProfilePicture(
        modifier = modifier,
        profilePictureData = ProfilePictureData.from(conversationPartner)
    )
}
