package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.saved_posts.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.saved_posts.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.ui.post.PostItemMainContent
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.ISavedPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.SavedPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.SavedPostPostingType
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.SavedPostStatus


@Composable
fun SavedPostItem(
    modifier: Modifier = Modifier,
    savedPost: ISavedPost,
    isLoading: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    trailingCardContent: (@Composable ColumnScope.() -> Unit)? = null,
) {
    ConversationContextInfo(
        modifier = modifier
            .combinedClickable(
                enabled = !isLoading,
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        conversation = savedPost.conversation,
        isThreadReply = savedPost.postingType == SavedPostPostingType.ANSWER,
        isLoading = isLoading,
        trailingCardContent = trailingCardContent
    ) {

        PostItemMainContent(
            modifier = Modifier,
            post = savedPost,
            onClick = onClick,
            onLongClick = onLongClick,
        )
    }
}

@Composable
private fun ConversationContextInfo(
    modifier: Modifier,
    conversation: ISavedPost.SimpleConversationInfo,
    isThreadReply: Boolean,
    isLoading: Boolean,
    trailingCardContent: (@Composable ColumnScope.() -> Unit)? = null,
    postContent: @Composable () -> Unit,
) {
    Card(
        modifier = modifier,
    ) {
        AnimatedVisibility(
            visible = isLoading,
        ) {
            LinearProgressIndicator(Modifier.fillMaxWidth(),)
        }

        Column(
            modifier = Modifier.padding(8.dp),
        ) {
            ConversationContextInfoHeader(
                modifier = Modifier.fillMaxWidth(),
                conversation = conversation,
                isThreadReply = isThreadReply,
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (!isThreadReply) {
                postContent()
                trailingCardContent?.invoke(this@Column)
                return@Card
            }

            val color = MaterialTheme.colorScheme.secondary

            Icon(
                modifier = Modifier.size(16.dp),
                imageVector = Icons.Default.MoreHoriz,
                contentDescription = null,
                tint = color,
            )

            Row(
                modifier.height(intrinsicSize = IntrinsicSize.Min)
            ) {
                VerticalDivider(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .width(2.dp),
                    color = color,
                )

                Spacer(Modifier.width(4.dp))

                postContent()
            }

            trailingCardContent?.invoke(this@Column)
        }
    }
}

@Composable
private fun ConversationContextInfoHeader(
    modifier: Modifier,
    conversation: ISavedPost.SimpleConversationInfo,
    isThreadReply: Boolean,
) {
    val contextText = when (conversation.type) {
        ISavedPost.SimpleConversationInfo.ConversationType.CHANNEL -> conversation.title
        ISavedPost.SimpleConversationInfo.ConversationType.DIRECT -> stringResource(id = R.string.saved_posts_context_private_chat)
    }

    val fullContextText = if (isThreadReply) {
        contextText + " > " + stringResource(id = R.string.saved_posts_context_thread)
    } else {
        contextText
    }

    val icon = when (conversation.type) {
        ISavedPost.SimpleConversationInfo.ConversationType.CHANNEL -> Icons.Default.Numbers
        ISavedPost.SimpleConversationInfo.ConversationType.DIRECT -> Icons.Default.Lock
    }

    val color = MaterialTheme.colorScheme.secondary

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            modifier = Modifier.size(16.dp),
            imageVector = icon,
            contentDescription = null,
            tint = color,
        )

        Text(
            text = fullContextText,
            style = MaterialTheme.typography.bodySmall,
            color = color,
        )
    }
}



@Preview
@Composable
private fun SavedPostItemPreview() {
    SavedPostItem(
        isLoading = true,
        savedPost = SavedPost(
            id = 1,
            referencePostId = 1,
            author = User(),
            content = "Content",
            savedPostStatus = SavedPostStatus.IN_PROGRESS,
            postingType = SavedPostPostingType.ANSWER,
            conversation = ISavedPost.SimpleConversationInfo(
                id = 1,
                title = "Title",
                type = ISavedPost.SimpleConversationInfo.ConversationType.CHANNEL,
            ),
        ),
        onClick = {},
        onLongClick = {},
    )
}