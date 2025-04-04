package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.ui.post

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.LocalLinkOpener
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.deeplinks.CommunicationDeeplinks
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.ui.ChatListItem
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.AnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IAnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.GroupChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.humanReadableName

const val TEST_TAG_FORWARDED_POST_COLUMN = "TEST_TAG_FORWARDED_POST_COLUMN"
const val TEST_TAG_DELETED_FORWARDED_POST = "TEST_TAG_DELETED_FORWARDED_POST"

fun testTagForForwardedPost(postId: Long?) = "forwarded-post$postId"

@Composable
fun ForwardedMessageColumn(
    modifier: Modifier,
    chatListItem: ChatListItem.PostItem.ForwardedMessage,
) {
    if (chatListItem.forwardedPosts.isEmpty()) return
    val forwardedPost = chatListItem.forwardedPosts[0]

    // Currently, only one forwarded post is supported
    Column(
        modifier = modifier.testTag(TEST_TAG_FORWARDED_POST_COLUMN),
    ) {
        ForwardedMessageItem(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTagForForwardedPost(forwardedPost?.serverPostId)),
            courseId = chatListItem.courseId,
            forwardedPost = forwardedPost,
        )
    }
}

@Composable
private fun ForwardedMessageItem(
    modifier: Modifier,
    courseId: Long,
    forwardedPost: IBasePost?,
    isPreview: Boolean = false,
) {
    QuotedMessageContainer(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacings.Post.innerSpacing)
        ) {
            if (!isPreview) {
                ForwardedMessageHeader(
                    modifier = Modifier,
                    forwardedPost = forwardedPost,
                    courseId = courseId,
                )
            }

            if (forwardedPost == null) {
                Text(
                    modifier = Modifier
                        .wrapContentHeight(unbounded = true)
                        .testTag(TEST_TAG_DELETED_FORWARDED_POST),
                    text = stringResource(R.string.post_forwarded_deleted),
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
                return@Column
            }

            PostItemMainContent(
                modifier = Modifier.wrapContentHeight(unbounded = true),
                post = forwardedPost,
                isRoleBadgeVisible = false,
                onClick = {},
                onLongClick = {},
            )
        }
    }
}

@Composable
private fun ForwardedMessageHeader(
    modifier: Modifier,
    forwardedPost: IBasePost?,
    courseId: Long,
) {
    val linkOpener = LocalLinkOpener.current
    val (sourceConversationId, sourceChannelText, conversationType) = resolveConversation(forwardedPost)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.height(14.dp),
            painter = painterResource(id = R.drawable.forward),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )

        Spacer(Modifier.width(Spacings.Post.innerSpacing))

        Text(
            text = stringResource(R.string.post_forwarded_from),
            style = MaterialTheme.typography.bodySmall,
        )

        Spacer(Modifier.width(2.dp))

        val isConversationPublic = conversationType == ConversationType.CHANNEL
        val isConversationClickable = isConversationPublic && sourceConversationId != null && forwardedPost != null
        Text(
            modifier = Modifier.clickable(enabled = isConversationClickable) {
                linkOpener.openLink(
                    CommunicationDeeplinks.ToConversation.inAppLink(
                        courseId = courseId,
                        conversationId = sourceConversationId ?: return@clickable
                    )
                )
            },
            text = sourceChannelText,
            color = if (isConversationClickable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
fun ForwardedMessagePreview(
    modifier: Modifier,
    forwardedPost: IBasePost?
) {
    ForwardedMessageItem(
        modifier = modifier,
        courseId = -1,
        forwardedPost = forwardedPost,
        isPreview = true
    )
}

@Composable
private fun resolveConversation(forwardedPost: IBasePost?): Triple<Long?, String, ConversationType> {
    val conversation = when (forwardedPost) {
        is StandalonePost -> forwardedPost.conversation
        is AnswerPost -> forwardedPost.post?.conversation
        else -> return Triple(null, stringResource(R.string.post_forwarded_from_default),
            ConversationType.UNKNOWN
        )
    }

    val isFromThread = forwardedPost is IAnswerPost
    val conversationId = conversation?.id

    return when (conversation) {
        is OneToOneChat -> {
            val message = if (isFromThread) R.string.post_forwarded_from_a_thread else R.string.post_forwarded_from_a_direct_message
           Triple(conversationId, stringResource(message, stringResource(R.string.post_forwarded_from_a_direct_message)),
               ConversationType.ONE_TO_ONE
           )
        }
        is GroupChat -> {
            val message = if (isFromThread) R.string.post_forwarded_from_a_thread else R.string.post_forwarded_from_a_group_chat
            Triple(conversationId, stringResource(message, stringResource(R.string.post_forwarded_from_a_group_chat)),
                ConversationType.GROUP
            )
        }
        else -> {
            val conversationName = conversation?.humanReadableName?.let { "#$it" } ?: stringResource(
                R.string.post_forwarded_from_default)
            if (isFromThread) Triple(conversationId, stringResource(R.string.post_forwarded_from_a_thread, conversationName),
                ConversationType.CHANNEL
            )
            else Triple(conversationId, conversationName, ConversationType.CHANNEL)
        }
    }
}

private enum class ConversationType {
    ONE_TO_ONE,
    GROUP,
    CHANNEL,
    UNKNOWN
}