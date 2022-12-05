package de.tum.informatics.www1.artemis.native_app.core.communication.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.placeholder.material.placeholder
import de.tum.informatics.www1.artemis.native_app.core.communication.R
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
    getUnicodeForEmojiId: @Composable (String) -> String
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
        getUnicodeForEmojiId = getUnicodeForEmojiId
    )
}

@Composable
internal fun AnswerPostItem(
    modifier: Modifier,
    answerPost: AnswerPost,
    getUnicodeForEmojiId: @Composable (String) -> String
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
        getUnicodeForEmojiId = getUnicodeForEmojiId
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
    getUnicodeForEmojiId: @Composable (String) -> String
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
                getUnicodeForEmojiId = getUnicodeForEmojiId
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

@Composable
private fun StandalonePostFooter(
    modifier: Modifier,
    isPlaceholder: Boolean,
    reactions: List<Post.Reaction>,
    postItemViewType: PostItemViewType,
    getUnicodeForEmojiId: @Composable (String) -> String
) {
    val reactionCount: Map<String, Int> = remember(reactions) {
        reactions.groupBy { it.emojiId }.mapValues { it.value.size }
    }

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
                        Text(text = "$count")
                    },
                    onClick = {

                    }
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
}

private class PostPreviewProvider : PreviewParameterProvider<Post?> {
    override val values: Sequence<Post?>
        get() {
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
                    Post.Reaction("rocket", 0, "I reacted")
                ),
                courseWideContext = null
            )

            return sequenceOf(
                null,
                basePost,
                basePost.copy(tags = emptyList(), title = null)
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
        getUnicodeForEmojiId = { "\uD83D\uDE80" }
    )
}