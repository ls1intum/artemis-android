package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.GroupChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.ProfilePicture
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.ProfilePictureData
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.ProfilePictureWithDialog

@Composable
fun ConversationIcon(
    modifier: Modifier = Modifier,
    conversation: Conversation,
    hasUnreadMessages: Boolean = false,
    allowFavoriteIndicator: Boolean = false,
    showDialogOnOneToOneChatClick: Boolean = false
) {
    Box {
        when (conversation) {
            is ChannelChat -> ChannelChatIcon(modifier, conversation)
            is GroupChat -> GroupChatIcon(modifier)
            is OneToOneChat -> OneToOneChatIcon(modifier, conversation, showDialogOnOneToOneChatClick)
        }

        if (hasUnreadMessages) {
            UnreadMessagesIndicator(
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }

        if (conversation.isFavorite && allowFavoriteIndicator) {
            FavoriteIndicator(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
            )
        }
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

fun getChannelIconImageVector(channelChat: ChannelChat): ImageVector {
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
    showDialogOnClick: Boolean = false
) {
    val conversationPartner = oneToOneChat.partner
    if (showDialogOnClick) {
        ProfilePictureWithDialog(
            modifier = modifier,
            courseUser = conversationPartner
        )
    } else {
        ProfilePicture(
            modifier = modifier,
            profilePictureData = ProfilePictureData.fromAccount(conversationPartner)
        )
    }
}

@Composable
private fun UnreadMessagesIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
    )
}

@Composable
private fun FavoriteIndicator(modifier: Modifier = Modifier) {
    Icon(
        modifier = modifier
            .size(8.dp),
        imageVector = Icons.Default.Favorite,
        contentDescription = "Favorite",
        tint = MaterialTheme.colorScheme.error
    )
}