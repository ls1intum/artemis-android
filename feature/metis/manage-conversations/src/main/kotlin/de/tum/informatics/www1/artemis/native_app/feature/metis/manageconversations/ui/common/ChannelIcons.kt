package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat


@Composable
internal fun ChannelIcons(channelChat: ChannelChat, hasUnreadMessages: Boolean = false) {
    Box {
        if (channelChat.isArchived || channelChat.isAnnouncementChannel) {
            ExtraChannelIcons(channelChat)
        } else {
            PrimaryChannelIcon(channelChat)
        }

        if (hasUnreadMessages) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(CommunicationColors.ArtemisBlue)
                    .align(Alignment.TopEnd)
            )
        }
    }
}



@Composable
internal fun ExtraChannelIcons(channelChat: ChannelChat) {
    Row {
        if (channelChat.isArchived) {
            Icon(
                imageVector = Icons.Default.Archive,
                contentDescription = null
            )
        }

        if (channelChat.isAnnouncementChannel) {
            Icon(
                imageVector = Icons.Default.Campaign,
                contentDescription = null
            )
        }
    }

}

@Composable
internal fun PrimaryChannelIcon(channelChat: ChannelChat) {
    Icon(
        imageVector = if (channelChat.isPublic) Icons.Default.Numbers else Icons.Default.Lock,
        contentDescription = null
    )
}

