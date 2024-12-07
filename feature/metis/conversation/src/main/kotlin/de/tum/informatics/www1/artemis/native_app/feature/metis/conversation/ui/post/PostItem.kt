package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.date.getRelativeTime
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.MarkdownText
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.CreatePostService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.getUnicodeForEmojiId
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IAnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IReaction
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IStandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.UserRole
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.ProfilePicture
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.ProfilePictureData
import io.github.fornewid.placeholder.material3.placeholder
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.koin.compose.koinInject

private val EditedGray: Color
    @Composable get() = Color.Gray

private val UnsentMessageTextColor: Color
    @Composable get() = Color.Gray

sealed class PostItemViewType {

    data class ChatListItem(
        val answerPosts: List<IAnswerPost>
    ) : PostItemViewType()

    data object ThreadContextPostItem : PostItemViewType()

    data object ThreadAnswerItem : PostItemViewType()
}

private const val PlaceholderContent = "WWWWWWW"

/**
 * Displays a post item or a placeholder for it.
 */
@Composable
internal fun PostItem(
    modifier: Modifier,
    post: IBasePost?,
    postItemViewType: PostItemViewType,
    clientId: Long,
    displayHeader: Boolean,
    onClickOnReaction: ((emojiId: String, create: Boolean) -> Unit)?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRequestRetrySend: () -> Unit
) {
    val isPlaceholder = post == null
    val isExpanded = when (postItemViewType) {
        PostItemViewType.ThreadContextPostItem -> true
        else -> false
    }

    // Retrieve post status
    val clientPostId = post?.clientPostId
    val postStatus = when {
        post == null || post.serverPostId != null || clientPostId == null -> CreatePostService.Status.FINISHED
        else -> {
            val createPostService: CreatePostService = koinInject()
            createPostService.observeCreatePostWorkStatus(clientPostId)
                .collectAsState(initial = CreatePostService.Status.PENDING)
                .value
        }
    }

    Column(
        modifier = modifier
            .let {
                if (postStatus == CreatePostService.Status.FAILED) {
                    it
                        .background(color = MaterialTheme.colorScheme.errorContainer)
                        .clickable(onClick = onRequestRetrySend)
                } else modifier
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick
                    )
            }
            .padding(PaddingValues(horizontal = Spacings.ScreenHorizontalSpacing)),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PostHeadline(
            modifier = Modifier.fillMaxWidth(),
            postStatus = postStatus,
            authorRole = post?.authorRole,
            authorName = post?.authorName,
            profilePictureData = ProfilePictureData.create(
                userId = post?.authorId,
                username = post?.authorName,
                imageUrl = post?.authorImageUrl
            ),
            creationDate = post?.creationDate,
            expanded = isExpanded,
            displayHeader = displayHeader
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    MarkdownText(
                        markdown = remember(post?.content, isPlaceholder) {
                            if (isPlaceholder) {
                                PlaceholderContent
                            } else post?.content.orEmpty()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .placeholder(visible = isPlaceholder),
                        style = MaterialTheme.typography.bodyMedium,
                        onClick = onClick,
                        onLongClick = onLongClick,
                        color = if (post?.serverPostId == null) UnsentMessageTextColor else Color.Unspecified
                    )

                    if (post?.updatedDate != null) {
                        Text(
                            text = stringResource(id = R.string.post_edited_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = EditedGray
                        )
                    }

                    when (post) {
                        is IStandalonePost -> {
                            if (post.resolved == true) {
                                ResolvedLabel(
                                    modifier = Modifier.fillMaxWidth(),
                                    resourceString = R.string.post_is_resolved
                                )
                            }
                        }
                        is IAnswerPost -> {
                            if (post.resolvesPost) {
                                ResolvedLabel(
                                    modifier = Modifier.fillMaxWidth(),
                                    resourceString = R.string.post_resolves
                                )
                            }
                        }
                        else -> {}
                    }
                }

                StandalonePostFooter(
                    modifier = Modifier.fillMaxWidth(),
                    clientId = clientId,
                    reactions = remember(post?.reactions) { post?.reactions.orEmpty() },
                    postItemViewType = postItemViewType,
                    onClickReaction = onClickOnReaction
                )

                if (!post?.reactions.isNullOrEmpty()) {
                    Box(modifier = Modifier.height(2.dp))
                }
            }
        }
    }
}

@Composable
private fun PostHeadline(
    modifier: Modifier,
    authorRole: UserRole?,
    authorName: String?,
    profilePictureData: ProfilePictureData,
    creationDate: Instant?,
    postStatus: CreatePostService.Status,
    expanded: Boolean = false,
    displayHeader: Boolean = true,
    content: @Composable () -> Unit
) {
    if (expanded) {
        Column(modifier = modifier) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HeadlineProfilePicture(
                    profilePictureData = profilePictureData,
                )

                HeadlineAuthorInfo(
                    modifier = Modifier.fillMaxWidth(),
                    authorName = authorName,
                    authorRole = authorRole,
                    creationDate = creationDate,
                    expanded = true
                )
            }

            content()
        }
    } else {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val doDisplayHeader = displayHeader || postStatus == CreatePostService.Status.FAILED

            HeadlineProfilePicture(
                profilePictureData = profilePictureData,
                displayImage = doDisplayHeader
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (postStatus == CreatePostService.Status.FAILED) {
                    Text(
                        text = stringResource(id = R.string.post_sending_failed),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (doDisplayHeader) {
                    HeadlineAuthorInfo(
                        modifier = Modifier.fillMaxWidth(),
                        authorName = authorName,
                        authorRole  = authorRole,
                        creationDate = creationDate,
                        expanded = false
                    )
                } else {
                    Box(modifier = Modifier.height(4.dp))
                }

                content()
            }
        }
    }
}

@Composable
private fun ResolvedLabel(
    modifier: Modifier,
    resourceString: Int
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            Icons.Default.Check,
            modifier = Modifier
                .size(16.dp)
                .fillMaxSize(),
            contentDescription = null
        )
        Text(
            text = stringResource(id = resourceString),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun HeadlineAuthorInfo(
    modifier: Modifier,
    authorName: String?,
    authorRole: UserRole?,
    creationDate: Instant?,
    expanded: Boolean
) {
    val relativeTimeTo = remember(creationDate) {
        creationDate ?: Clock.System.now()
    }

    val creationDateContent: @Composable () -> Unit = {
        val relativeTime = getRelativeTime(to = relativeTimeTo, showDate = false)

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = remember(relativeTime) { relativeTime.toString() },
            style = MaterialTheme.typography.bodySmall
        )
    }

    if (expanded) {
        Column(modifier) {
            AuthorRoleAndNameRow(authorRole, authorName)
            creationDateContent()
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            AuthorRoleAndNameRow(authorRole, authorName)
            creationDateContent()
        }
    }
}

@Composable
private fun AuthorRoleAndNameRow(
    authorRole: UserRole?,
    authorName: String?
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HeadlineAuthorIcon(authorRole)

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            modifier = Modifier,
            text = remember(authorName) { authorName ?: "Placeholder" },
            maxLines = 1,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun HeadlineProfilePicture(
    profilePictureData: ProfilePictureData,
    displayImage: Boolean = true
) {
    val size = 30.dp
    Box(modifier = Modifier.size(size)) {
        if (!displayImage) {
            return
        }

        ProfilePicture(
            modifier = Modifier
                .size(size)
                .clip(MaterialTheme.shapes.extraSmall),
            profilePictureData = profilePictureData,
        )
    }
}

@Composable
private fun HeadlineAuthorIcon(
    authorRole: UserRole?,
) {
    val icon = when (authorRole) {
        UserRole.INSTRUCTOR -> Icons.Default.School
        UserRole.TUTOR -> Icons.Default.SupervisorAccount
        UserRole.USER -> Icons.Default.Person
        null -> Icons.Default.Person
    }

    Icon(
        modifier = Modifier.size(16.dp),
        imageVector = icon,
        contentDescription = null
    )
}

/**
 * Display the tags, the reactions and the action buttons like reply, view replies and react with emoji.
 */
@Composable
private fun StandalonePostFooter(
    modifier: Modifier,
    clientId: Long,
    reactions: List<IReaction>,
    postItemViewType: PostItemViewType,
    onClickReaction: ((emojiId: String, create: Boolean) -> Unit)?
) {
    val reactionCount: Map<String, ReactionData> = remember(reactions, clientId) {
        reactions.groupBy { it.emojiId }.mapValues { groupedReactions ->
            ReactionData(
                groupedReactions.value.size,
                groupedReactions.value.any { it.creatorId == clientId }
            )
        }
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            reactionCount.forEach { (emoji, reactionData) ->
                EmojiChip(
                    selected = reactionData.hasClientReacted,
                    emojiId = emoji,
                    reactionCount = reactionData.reactionCount,
                    onClick = {
                        onClickReaction?.invoke(emoji, !reactionData.hasClientReacted)
                    }
                )
            }
        }

        if (postItemViewType is PostItemViewType.ChatListItem) {
            val replyCount = postItemViewType.answerPosts.size

            if (replyCount > 0) {
                Text(
                    style = MaterialTheme.typography.bodyMedium,
                    text = pluralStringResource(
                        id = R.plurals.communication_standalone_post_view_replies_button,
                        count = replyCount,
                        replyCount
                    ),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

private data class ReactionData(val reactionCount: Int, val hasClientReacted: Boolean)

@Composable
private fun AnimatedCounter(currentCount: Int) {
    AnimatedContent(
        targetState = currentCount,
        transitionSpec = {
            if (targetState > initialState) {
                slideInVertically { height -> height } + fadeIn() togetherWith
                        slideOutVertically { height -> -height } + fadeOut()
            } else {
                slideInVertically { height -> -height } + fadeIn() togetherWith
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

@Composable
private fun EmojiChip(
    modifier: Modifier = Modifier,
    selected: Boolean,
    emojiId: String,
    reactionCount: Int,
    onClick: () -> Unit
) {
    val shape = CircleShape

    val backgroundColor =
        if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent

    Box(
        modifier = modifier
            .background(color = backgroundColor, shape)
            .clip(shape)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = getUnicodeForEmojiId(emojiId = emojiId),
                fontSize = 14.sp
            )

            AnimatedCounter(reactionCount)
        }
    }
}
