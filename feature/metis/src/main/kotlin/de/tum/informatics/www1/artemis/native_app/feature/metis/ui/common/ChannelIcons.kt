package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.common

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.ChannelChat

@Composable
internal fun ChannelIcons(channelChat: ChannelChat) {
    Row {
        PrimaryChannelIcon(channelChat)

        ExtraChannelIcons(channelChat)
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

