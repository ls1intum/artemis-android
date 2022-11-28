package de.tum.informatics.www1.artemis.native_app.core.communication.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import coil.ImageLoader
import com.google.accompanist.placeholder.material.placeholder
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisContext
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.Post
import de.tum.informatics.www1.artemis.native_app.core.datastore.room.model.metis.BasePostingEntity
import de.tum.informatics.www1.artemis.native_app.core.ui.date.getRelativeTime
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.Duration.Companion.minutes

@Composable
fun MetisUi(modifier: Modifier, metisContext: MetisContext) {
    val viewModel: MetisViewModel = koinViewModel { parametersOf(metisContext) }

    val posts: LazyPagingItems<Post> = viewModel.postPagingData.collectAsLazyPagingItems()

    Column(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 8.dp)
        ) {
            items(posts) { post ->
                PostItem(modifier = Modifier.fillMaxWidth(), post = post, onClickReply = {})
            }
        }
    }
}

/**
 * Displays a post item or a placeholder for it.
 */
@Composable
private fun PostItem(modifier: Modifier, post: Post?, onClickReply: () -> Unit) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PostHeadline(
                modifier = Modifier.fillMaxWidth(),
                isPlaceholder = post == null,
                authorRole = post?.authorRole,
                authorName = post?.authorName,
                creationDate = post?.creationDate
            )

            if (post?.title != null || post == null) {
                Text(
                    text = post?.title ?: "W".repeat(15),
                    modifier = Modifier
                        .fillMaxWidth()
                        .placeholder(post == null),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            MarkdownText(
                modifier = Modifier
                    .fillMaxWidth()
                    .placeholder(post == null),
                markdown = post?.content ?: "W".repeat(40),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 5,
                imageLoader = ImageLoader()
            )

            StandalonePostFooter(
                modifier = Modifier.fillMaxWidth(),
                isPlaceholder = post == null,
                reactions = post?.reactions.orEmpty(),
                onClickReply = onClickReply
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
    onClickReply: () -> Unit
) {
    val reactionCount: Map<String, Int> = remember(reactions) {
        reactions.groupBy { it.emojiId }.mapValues { it.value.size }
    }

    Row(modifier = modifier.placeholder(isPlaceholder)) {
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState())
        ) {
            reactionCount.forEach { (emoji, count) ->
                AssistChip(
                    label = {
                        Text(text = "$emoji $count")
                    },
                    onClick = {

                    }
                )
            }
        }

        IconButton(onClick = onClickReply) {
            Icon(imageVector = Icons.Default.Reply, contentDescription = null)
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
        onClickReply = {}
    )
}