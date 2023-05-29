package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.outlined.AddReaction
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.accompanist.placeholder.material.placeholder
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.IReaction
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.UserRole
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.date.getRelativeTime
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.MarkdownText
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.AnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.emoji.EmojiView
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.emoji.emoji_picker.EmojiPicker
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.getUnicodeForEmojiId
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

sealed class PostItemViewType {

    data class ChatListItem(
        val answerPosts: List<AnswerPost>
    ) : PostItemViewType()

    object ThreadItem : PostItemViewType()

    object ThreadAnswerItem : PostItemViewType()
}

/**
 * Displays a post item or a placeholder for it.
 */
@Composable
internal fun PostItem(
    modifier: Modifier,
    post: IBasePost?,
    postItemViewType: PostItemViewType,
    clientId: Long,
    onRequestReactWithEmoji: () -> Unit,
    onClickOnPresentReaction: (emojiId: String) -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val isPlaceholder = post == null

    Column(
        modifier = modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(PaddingValues(horizontal = Spacings.ScreenHorizontalSpacing)),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PostHeadline(
            modifier = Modifier.fillMaxWidth(),
            isPlaceholder = isPlaceholder,
            authorRole = post?.authorRole,
            authorName = post?.authorName,
            creationDate = post?.creationDate
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                MarkdownText(
                    markdown = post?.content ?: "W".repeat(40),
                    modifier = Modifier
                        .fillMaxWidth()
                        .placeholder(visible = isPlaceholder),
                    maxLines = 5,
                    style = MaterialTheme.typography.bodyMedium,
                    onClick = onClick,
                    onLongClick = onLongClick
                )

                StandalonePostFooter(
                    modifier = Modifier.fillMaxWidth(),
                    isPlaceholder = isPlaceholder,
                    reactions = post?.reactions.orEmpty(),
                    postItemViewType = postItemViewType,
                    onRequestReactWithEmoji = onRequestReactWithEmoji,
                    onClickReaction = onClickOnPresentReaction,
                    clientId = clientId
                )
            }
        }
    }
}

@Composable
private fun PostHeadline(
    modifier: Modifier,
    isPlaceholder: Boolean,
    authorRole: UserRole?,
    authorName: String?,
    creationDate: Instant?,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val icon = when (authorRole) {
            UserRole.INSTRUCTOR -> Icons.Default.School
            UserRole.TUTOR -> Icons.Default.SupervisorAccount
            UserRole.USER -> Icons.Default.Person
            null -> Icons.Default.Person
        }

        Icon(
            modifier = Modifier
                .size(30.dp)
                .placeholder(visible = isPlaceholder),
            imageVector = icon,
            contentDescription = null
        )

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    modifier = Modifier.placeholder(visible = isPlaceholder),
                    text = authorName ?: "Placeholder",
                    maxLines = 1,
                    style = MaterialTheme.typography.titleSmall
                )

                val relativeTimeTo = remember(creationDate) {
                    creationDate ?: Clock.System.now()
                }

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .placeholder(visible = isPlaceholder),
                    text = getRelativeTime(to = relativeTimeTo).toString(),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            content()
        }
    }
}

/**
 * Display the tags, the reactions and the action buttons like reply, view replies and react with emoji.
 */
@Composable
private fun StandalonePostFooter(
    modifier: Modifier,
    isPlaceholder: Boolean,
    clientId: Long,
    reactions: List<IReaction>,
    postItemViewType: PostItemViewType,
    onRequestReactWithEmoji: () -> Unit,
    onClickReaction: (emojiId: String) -> Unit
) {
    val reactionCount: Map<String, ReactionData> = remember(reactions, clientId) {
        reactions.groupBy { it.emojiId }.mapValues { groupedReactions ->
            ReactionData(
                groupedReactions.value.size,
                groupedReactions.value.any { it.creatorId == clientId }
            )
        }
    }

    Row(modifier = modifier.placeholder(isPlaceholder)) {
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            reactionCount.forEach { (emoji, reactionData) ->
                InputChip(
                    selected = reactionData.hasClientReacted,
                    leadingIcon = {
                        EmojiView(
                            modifier = Modifier,
                            emojiUnicode = getUnicodeForEmojiId(emojiId = emoji),
                            emojiFontSize = 16.sp
                        )
                    },
                    label = {
                        AnimatedCounter(reactionData.reactionCount)
                    },
                    onClick = {
                        onClickReaction(emoji)
                    }
                )
            }

            IconButton(onClick = onRequestReactWithEmoji) {
                Icon(
                    imageVector = Icons.Outlined.AddReaction,
                    contentDescription = null
                )
            }
        }

        if (postItemViewType is PostItemViewType.ChatListItem) {
            val replyCount = postItemViewType.answerPosts.size

            if (replyCount > 0) {
                Text(
                    text = pluralStringResource(
                        id = R.plurals.communication_standalone_post_view_replies_button,
                        count = replyCount,
                        replyCount
                    )
                )
            }
        }
    }
}

private data class ReactionData(val reactionCount: Int, val hasClientReacted: Boolean)

@Composable
private fun EmojiDialog(
    onDismissRequest: () -> Unit,
    onSelectEmoji: (emojiId: String) -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(5))
        ) {
            EmojiPicker(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
                    .padding(horizontal = 8.dp),
                onSelectEmoji = onSelectEmoji
            )
        }
    }
}

@Composable
private fun AnimatedCounter(currentCount: Int) {
    AnimatedContent(
        targetState = currentCount,
        transitionSpec = {
            if (targetState > initialState) {
                slideInVertically { height -> height } + fadeIn() with
                        slideOutVertically { height -> -height } + fadeOut()
            } else {
                slideInVertically { height -> -height } + fadeIn() with
                        slideOutVertically { height -> height } + fadeOut()
            }.using(
                SizeTransform(clip = false)
            )
        },
        label = "Animate reaction count change"
    ) { targetCount ->
        Text(text = "$targetCount")
    }
}
