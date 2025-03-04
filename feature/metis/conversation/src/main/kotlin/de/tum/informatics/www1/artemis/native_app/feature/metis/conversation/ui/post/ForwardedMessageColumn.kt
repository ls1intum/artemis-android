package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.LocalLinkOpener
import de.tum.informatics.www1.artemis.native_app.core.ui.deeplinks.CommunicationDeeplinks
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist.ChatListItem
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.AnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IAnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IStandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.GroupChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.humanReadableName

@Composable
fun ForwardedMessageColumn(
    modifier: Modifier,
    chatListItem: ChatListItem.PostItem.ForwardedMessage,
) {
    val forwardedPosts = chatListItem.forwardedPosts

    Column(
        modifier = modifier,
    ) {
        forwardedPosts.forEach{ forwardedPost ->
            ForwardedMessageItem(
                modifier = Modifier.fillMaxWidth(),
                courseId = chatListItem.courseId,
                forwardedPost = forwardedPost,
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ForwardedMessageItem(
    modifier: Modifier,
    courseId: Long,
    forwardedPost: IBasePost,
) {
    val linkOpener = LocalLinkOpener.current
    val (sourceConversationId, sourceChannelText) = resolveConversation(forwardedPost)

    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(shape = CircleShape)
                .fillMaxHeight()
                .width(6.dp)
                .background(color = MaterialTheme.colorScheme.primary)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    modifier = Modifier.height(14.dp),
                    painter = painterResource(id = R.drawable.forward),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = stringResource(R.string.post_forwarded_from),
                    style = MaterialTheme.typography.bodySmall,
                )

                Spacer(Modifier.width(2.dp))

                Text(
                    modifier = Modifier.clickable {
                        linkOpener.openLink(CommunicationDeeplinks.ToConversation.inAppLink(
                            courseId = courseId,
                            conversationId = sourceConversationId ?: return@clickable
                        ))
                    },
                    text = sourceChannelText,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            PostItemMainContent(
                modifier = Modifier.wrapContentHeight(unbounded = true),
                post = forwardedPost,
                onClick = {},
                onLongClick = {},
            )
        }
    }
}

@Composable
private fun resolveConversation(forwardedPost: IBasePost): Pair<Long?, String> {
    val conversation = when (forwardedPost) {
        is IStandalonePost ->  (forwardedPost as StandalonePost).conversation
        is IAnswerPost -> (forwardedPost as AnswerPost).post?.conversation
        else -> return null to stringResource(R.string.post_forwarded_from_default)
    }

    if (conversation is OneToOneChat) {
        return conversation.id to stringResource(R.string.post_forwarded_from_a_direct_message)
    }
    if (conversation is GroupChat) {
        return conversation.id to stringResource(R.string.post_forwarded_from_a_group_chat)
    }

    var conversationName = conversation?.humanReadableName ?: stringResource(R.string.post_forwarded_from_default)
    if (conversation != null) conversationName = "#$conversationName"
    val conversationId = conversation?.id

    if (forwardedPost is IAnswerPost) {
        return conversationId to stringResource(R.string.post_forwarded_from_a_thread, conversationName)
    }

    return conversationId to conversationName
}
