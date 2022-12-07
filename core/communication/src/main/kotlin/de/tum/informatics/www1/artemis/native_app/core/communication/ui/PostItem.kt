package de.tum.informatics.www1.artemis.native_app.core.communication.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.outlined.AddReaction
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.accompanist.placeholder.material.placeholder
import de.tum.informatics.www1.artemis.native_app.core.communication.R
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.emoji_picker.EmojiPicker
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.AnswerPost
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.Post
import de.tum.informatics.www1.artemis.native_app.core.datastore.room.model.metis.BasePostingEntity
import de.tum.informatics.www1.artemis.native_app.core.ui.date.getRelativeTime
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.MarkdownText
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.minutes

sealed class PostItemViewType {
    object StandaloneView : PostItemViewType()

    object AnswerItem : PostItemViewType()

    class StandaloneListItem(
        val answerPosts: List<AnswerPost>,
        val onClickReply: () -> Unit,
        val onClickViewReplies: () -> Unit
    ) : PostItemViewType()
}

/**
 * Displays a post item or a placeholder for it.
 */
@Composable
internal fun PostItem(
    modifier: Modifier,
    post: Post?,
    postItemViewType: PostItemViewType,
    getUnicodeForEmojiId: @Composable (String) -> String,
    onReactWithEmoji: (emojiId: String) -> Unit,
    onClickOnPresentReaction: (emojiId: String) -> Unit
) {
    PostItemBase(
        modifier = modifier,
        isPlaceholder = post == null,
        authorRole = post?.authorRole,
        authorName = post?.authorName,
        creationDate = post?.creationDate,
        title = post?.title,
        content = post?.content,
        reactions = post?.reactions.orEmpty(),
        postItemViewType = postItemViewType,
        getUnicodeForEmojiId = getUnicodeForEmojiId,
        onReactWithEmoji = onReactWithEmoji,
        onClickOnPresentReaction = onClickOnPresentReaction
    )
}

@Composable
internal fun AnswerPostItem(
    modifier: Modifier,
    answerPost: AnswerPost,
    getUnicodeForEmojiId: @Composable (String) -> String,
    onReactWithEmoji: (emojiId: String) -> Unit,
    onClickOnPresentReaction: (emojiId: String) -> Unit
) {
    PostItemBase(
        modifier = modifier,
        isPlaceholder = false,
        authorRole = answerPost.authorRole,
        authorName = answerPost.authorName,
        creationDate = answerPost.creationDate,
        title = null,
        content = answerPost.content,
        reactions = answerPost.reactions,
        postItemViewType = PostItemViewType.AnswerItem,
        getUnicodeForEmojiId = getUnicodeForEmojiId,
        onReactWithEmoji = onReactWithEmoji,
        onClickOnPresentReaction = onClickOnPresentReaction
    )
}

@Composable
private fun PostItemBase(
    modifier: Modifier,
    isPlaceholder: Boolean,
    authorRole: BasePostingEntity.UserRole?,
    authorName: String?,
    creationDate: Instant?,
    title: String?,
    content: String?,
    reactions: List<Post.Reaction>,
    postItemViewType: PostItemViewType,
    getUnicodeForEmojiId: @Composable (String) -> String,
    onReactWithEmoji: (emojiId: String) -> Unit,
    onClickOnPresentReaction: (emojiId: String) -> Unit
) {
    OutlinedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PostHeadline(
                modifier = Modifier.fillMaxWidth(),
                isPlaceholder = isPlaceholder,
                authorRole = authorRole,
                authorName = authorName,
                creationDate = creationDate
            )

            if (title != null || isPlaceholder) {
                Text(
                    text = title ?: "W".repeat(15),
                    modifier = Modifier
                        .fillMaxWidth()
                        .placeholder(isPlaceholder),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            MarkdownText(
                modifier = Modifier
                    .fillMaxWidth()
                    .placeholder(isPlaceholder),
                markdown = content ?: "W".repeat(40),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 5
            )

            StandalonePostFooter(
                modifier = Modifier.fillMaxWidth(),
                isPlaceholder = isPlaceholder,
                reactions = reactions,
                postItemViewType = postItemViewType,
                getUnicodeForEmojiId = getUnicodeForEmojiId,
                onReactWithEmoji = onReactWithEmoji,
                onClickReaction = onClickOnPresentReaction
            )
        }
    }
}

@Composable
private fun PostHeadline(
    modifier: Modifier,
    isPlaceholder: Boolean,
    authorRole: BasePostingEntity.UserRole?,
    authorName: String?,
    creationDate: Instant?
) {
    Row(
        modifier = modifier
    ) {
        val icon = when (authorRole) {
            BasePostingEntity.UserRole.INSTRUCTOR -> Icons.Default.School
            BasePostingEntity.UserRole.TUTOR -> Icons.Default.SupervisorAccount
            BasePostingEntity.UserRole.USER -> Icons.Default.Person
            null -> Icons.Default.Person
        }

        Icon(
            modifier = Modifier
                .size(40.dp)
                .placeholder(visible = isPlaceholder),
            imageVector = icon,
            contentDescription = null
        )

        Column(modifier = Modifier.padding(horizontal = 8.dp)) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .placeholder(visible = isPlaceholder),
                text = authorName ?: "Placeholder",
                maxLines = 1,
                style = MaterialTheme.typography.titleSmall
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .placeholder(visible = isPlaceholder),
                text = getRelativeTime(to = creationDate ?: Clock.System.now()).toString(),
                style = MaterialTheme.typography.bodySmall
            )
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
    reactions: List<Post.Reaction>,
    postItemViewType: PostItemViewType,
    getUnicodeForEmojiId: @Composable (String) -> String,
    onReactWithEmoji: (emojiId: String) -> Unit,
    onClickReaction: (emojiId: String) -> Unit
) {
    val reactionCount: Map<String, Int> = remember(reactions) {
        reactions.groupBy { it.emojiId }.mapValues { it.value.size }
    }

    var displayEmojiPicker: Boolean by remember { mutableStateOf(false) }

    Row(modifier = modifier.placeholder(isPlaceholder)) {
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            reactionCount.forEach { (emoji, count) ->
                AssistChip(
                    leadingIcon = {
                        Text(
                            text = getUnicodeForEmojiId(emoji),
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    },
                    label = {
                        AnimatedCounter(count)
                    },
                    onClick = {
                        onClickReaction(emoji)
                    }
                )
            }

            IconButton(onClick = { displayEmojiPicker = true }) {
                Icon(
                    imageVector = Icons.Outlined.AddReaction,
                    contentDescription = null
                )
            }
        }

        if (postItemViewType is PostItemViewType.StandaloneListItem) {
            val replyCount = postItemViewType.answerPosts.size

            if (replyCount > 0) {
                TextButton(onClick = postItemViewType.onClickViewReplies) {
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

        if (postItemViewType is PostItemViewType.StandaloneListItem) {
            IconButton(onClick = postItemViewType.onClickReply) {
                Icon(imageVector = Icons.Default.Reply, contentDescription = null)
            }
        }
    }

    if (displayEmojiPicker) {
        EmojiDialog(
            onDismissRequest = {
                displayEmojiPicker = false

            },
            onSelectEmoji = { emojiId ->
                displayEmojiPicker = false
                onReactWithEmoji(emojiId)
            }
        )
    }
}

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
        }
    ) { targetCount ->
        Text(text = "$targetCount")
    }
}

private class PostPreviewProvider : PreviewParameterProvider<Post?> {
    override val values: Sequence<Post?>
        get() {
            val baseReaction = Post.Reaction("rocket", 0, "I reacted", 0)

            val basePost = Post(
                clientPostId = "",
                serverPostId = 0,
                title = "Example title",
                content = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.",
                authorName = "Max Mustermann",
                authorRole = BasePostingEntity.UserRole.USER,
                creationDate = Clock.System.now().minus(5.minutes),
                resolved = false,
                tags = listOf("Very", "Important"),
                answerPostings = emptyList(),
                reactions = listOf(
                    baseReaction
                ),
                courseWideContext = null
            )

            return sequenceOf(
                null,
                basePost,
                basePost.copy(tags = emptyList(), title = null),
                basePost.copy(
                    reactions = listOf(
                        baseReaction.copy(emojiId = "rocket"),
                        baseReaction.copy(emojiId = "older_woman"),
                        baseReaction.copy(emojiId = "red_haired_woman"),
                        baseReaction.copy(emojiId = "bone"),
                        baseReaction.copy(emojiId = "eyes"),
                        baseReaction.copy(emojiId = "female_vampire")
                    )
                )
            )
        }

}

@Preview
@Composable
private fun PostPreview(
    @PreviewParameter(provider = PostPreviewProvider::class) post: Post?
) {
    PostItem(
        modifier = Modifier.fillMaxWidth(),
        post = post,
        postItemViewType = PostItemViewType.StandaloneListItem(
            post?.answerPostings.orEmpty(),
            {},
            {}
        ),
        getUnicodeForEmojiId = { "\uD83D\uDE80" },
        onReactWithEmoji = {},
        onClickOnPresentReaction = {}
    )
}