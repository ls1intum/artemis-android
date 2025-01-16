package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.InsertEmoticon
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.date.DateFormats
import de.tum.informatics.www1.artemis.native_app.core.ui.date.format
import de.tum.informatics.www1.artemis.native_app.core.ui.date.getRelativeTime
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.MarkdownText
import de.tum.informatics.www1.artemis.native_app.core.ui.material.colors.PostColors
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.CreatePostService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.getUnicodeForEmojiId
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions.EmojiDialog
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions.PostActions
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.DisplayPriority
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IAnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IReaction
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IStandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.UserRole
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.ProfilePictureWithDialog
import io.github.fornewid.placeholder.material3.placeholder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.koin.compose.koinInject

sealed class PostItemViewType {

    data class ChatListItem(
        val answerPosts: List<IAnswerPost>
    ) : PostItemViewType()

    data object ThreadContextPostItem : PostItemViewType()

    data object ThreadAnswerItem : PostItemViewType()
}

private const val PlaceholderContent = "WWWWWWW"
private val postHeadlineHeight = 36.dp
private val emojiHeight = 27.dp

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
    postItemViewJoinedType: PostItemViewJoinedType,
    isMarkedAsDeleteList: SnapshotStateList<IBasePost>,
    postActions: PostActions,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRequestRetrySend: () -> Unit
) {
    val isPlaceholder = post == null
    val isExpanded = when (postItemViewType) {
        PostItemViewType.ThreadContextPostItem -> true
        else -> false
    }
    val isDeleting by remember(post) { derivedStateOf { isMarkedAsDeleteList.contains(post) } }


    val isPinned = post is IStandalonePost && post.displayPriority == DisplayPriority.PINNED
    val hasFooter = (post is IStandalonePost && post.answers.orEmpty()
        .isNotEmpty()) || post?.reactions.orEmpty().isNotEmpty() || isExpanded

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
                } else {
                    it
                        .combinedClickable(
                            onClick = onClick,
                            onLongClick = onLongClick
                        )
                }
            }
            .padding(PaddingValues(horizontal = Spacings.ScreenHorizontalInnerSpacing))
    ) {
        val applyDistancePaddingToModifier: @Composable (Modifier) -> Modifier = {
            if (postItemViewJoinedType in listOf(
                    PostItemViewJoinedType.JOINED,
                    PostItemViewJoinedType.FOOTER
                )
            ) {
                it.padding(top = Spacings.Post.innerSpacing)
            } else {
                it.padding(bottom = 4.dp)
            }
        }

        if (isPinned) {
            IconLabel(
                modifier = applyDistancePaddingToModifier(Modifier)
                    .fillMaxWidth(),
                resourceString = R.string.post_is_pinned,
                icon = Icons.Outlined.PushPin
            )
        }

        if (post is IAnswerPost && post.resolvesPost) {
            IconLabel(
                modifier = applyDistancePaddingToModifier(Modifier)
                    .fillMaxWidth(),
                resourceString = R.string.post_resolves,
                icon = Icons.Default.Check
            )
        }

        PostHeadline(
            modifier = Modifier.fillMaxWidth(),
            postStatus = postStatus,
            authorRole = post?.authorRole,
            authorName = post?.authorName,
            authorId = post?.authorId ?: -1,
            authorImageUrl = post?.authorImageUrl,
            creationDate = post?.creationDate,
            expanded = isExpanded,
            isAnswerPost = post is IAnswerPost,
            displayHeader = displayHeader,
            isDeleting = isDeleting
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isDeleting) {
                    UndoDeleteHeader(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            postActions.requestUndoDeletePost?.invoke()
                        }
                    )
                    return@PostHeadline
                }

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
                    color = if (post?.serverPostId == null) PostColors.unsentMessageText else Color.Unspecified
                )

                val instant = post?.updatedDate
                if (instant != null) {
                    val updateTime = instant.format(DateFormats.EditTimestamp.format)
                    Spacer(modifier = Modifier.height(Spacings.Post.innerSpacing))

                    Text(
                        text = stringResource(id = R.string.post_edited_hint, updateTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = PostColors.editedHintText
                    )
                }

                if (post is IStandalonePost && post.resolved == true) {
                    Spacer(modifier = Modifier.height(Spacings.Post.innerSpacing))

                    IconLabel(
                        modifier = Modifier.fillMaxWidth(),
                        resourceString = R.string.post_is_resolved,
                        icon = Icons.Default.Check
                    )
                }

                if (hasFooter) {
                    Spacer(modifier = Modifier.height(Spacings.Post.innerSpacing))
                }

                StandalonePostFooter(
                    modifier = Modifier
                        .fillMaxWidth()
                        .let {
                            if (postItemViewJoinedType in listOf(
                                    PostItemViewJoinedType.JOINED,
                                    PostItemViewJoinedType.HEADER
                                ) && post?.reactions
                                    .orEmpty()
                                    .isNotEmpty()
                            ) {
                                it.padding(bottom = Spacings.Post.innerSpacing)
                            } else {
                                it
                            }
                        },
                    clientId = clientId,
                    reactions = remember(post?.reactions) { post?.reactions.orEmpty() },
                    postItemViewType = postItemViewType,
                    postActions = postActions
                )
            }
        }
    }
}

@Composable
private fun PostHeadline(
    modifier: Modifier,
    authorRole: UserRole?,
    authorName: String?,
    authorId: Long,
    authorImageUrl: String?,
    creationDate: Instant?,
    postStatus: CreatePostService.Status,
    expanded: Boolean = false,
    isAnswerPost: Boolean,
    displayHeader: Boolean = true,
    isDeleting: Boolean,
    content: @Composable () -> Unit
) {
    val doDisplayHeader = displayHeader || postStatus == CreatePostService.Status.FAILED

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacings.Post.innerSpacing)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacings.Post.innerSpacing)
        ) {
            if (!doDisplayHeader) {
                return@Row
            }

            HeadlineProfilePicture(
                userId = authorId,
                userName = authorName.orEmpty(),
                imageUrl = authorImageUrl,
                userRole = authorRole,
                isGrayscale = isDeleting
            )

            if (postStatus == CreatePostService.Status.FAILED) {
                Text(
                    text = stringResource(id = R.string.post_sending_failed),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            HeadlineAuthorInfo(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(postHeadlineHeight),
                authorName = authorName,
                authorRole = authorRole,
                creationDate = creationDate,
                expanded = expanded,
                isAnswerPost = isAnswerPost,
                isGrayscale = isDeleting
            )
        }

        content()
    }
}

@Composable
private fun IconLabel(
    modifier: Modifier,
    resourceString: Int,
    icon: ImageVector
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
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
    expanded: Boolean,
    isAnswerPost: Boolean,
    isGrayscale: Boolean
) {
    Column(modifier = modifier) {
        AuthorRoleAndTimeRow(
            expanded = expanded,
            authorRole = authorRole,
            creationDate = creationDate,
            isAnswerPost = isAnswerPost,
            isGrayscale = isGrayscale
        )

        Spacer(modifier = Modifier.weight(1f))

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
private fun AuthorRoleAndTimeRow(
    expanded: Boolean,
    authorRole: UserRole?,
    creationDate: Instant?,
    isAnswerPost: Boolean,
    isGrayscale: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val relativeTimeTo = remember(creationDate) {
            creationDate ?: Clock.System.now()
        }

        val creationDateContent: @Composable () -> Unit = {

            val relativeTime = if (expanded || isAnswerPost) {
                getRelativeTime(to = relativeTimeTo, showDateAndTime = true)
            } else {
                getRelativeTime(to = relativeTimeTo, showDate = false)
            }

            Text(
                modifier = Modifier,
                text = relativeTime.toString(),
                style = MaterialTheme.typography.bodySmall
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeadlineAuthorRoleBadge(authorRole, isGrayscale)
            Spacer(modifier = Modifier.weight(1f))
            creationDateContent()
        }
    }
}

@Composable
private fun HeadlineProfilePicture(
    userId: Long,
    userName: String,
    imageUrl: String?,
    userRole: UserRole?,
    displayImage: Boolean = true,
    isGrayscale: Boolean = false
) {
    val size = postHeadlineHeight
    Box(
        modifier = Modifier
            .size(size)
            .applyGrayscale(isGrayscale)
    ) {
        if (!displayImage) {
            return
        }

        ProfilePictureWithDialog(
            modifier = Modifier.size(size),
            userId = userId,
            userName = userName,
            userRole = userRole,
            imageUrl = imageUrl,
        )
    }
}

@Composable
private fun HeadlineAuthorRoleBadge(
    authorRole: UserRole?,
    isGrayscale: Boolean
) {
    /*
    * remember is needed here to prevent the value from being reset after an update
    * (the author role is not sent when updating a post)
    */
    val initialAuthorRole = remember { mutableStateOf(authorRole) }
    val (text, color) = when (initialAuthorRole.value) {
        UserRole.INSTRUCTOR -> R.string.post_instructor to PostColors.Roles.instructor
        UserRole.TUTOR -> R.string.post_tutor to PostColors.Roles.tutor
        UserRole.USER -> R.string.post_student to PostColors.Roles.student
        null -> R.string.post_student to PostColors.Roles.student
    }

    Box(
        modifier = Modifier
            .background(color, MaterialTheme.shapes.extraSmall)
            .applyGrayscale(isGrayscale)
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 1.dp),
            text = stringResource(id = text),
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
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
    postActions: PostActions
) {
    val reactionCount: Map<String, ReactionData> = remember(reactions, clientId) {
        reactions.groupBy { it.emojiId }.mapValues { groupedReactions ->
            ReactionData(
                groupedReactions.value.size,
                groupedReactions.value.any { it.creatorId == clientId }
            )
        }
    }
    val showEmojiDialog = remember { mutableStateOf(false) }

    if (showEmojiDialog.value) {
        EmojiDialog(
            onDismissRequest = { showEmojiDialog.value = false },
            onSelectEmoji = { emojiId ->
                postActions.onClickReaction?.invoke(emojiId, true)
                showEmojiDialog.value = false
            }
        )
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            reactionCount.forEach { (emoji, reactionData) ->
                EmojiChip(
                    selected = reactionData.hasClientReacted,
                    emojiId = emoji,
                    reactionCount = reactionData.reactionCount,
                    onClick = {
                        postActions.onClickReaction?.invoke(emoji, !reactionData.hasClientReacted)
                    }
                )
            }
            if (reactionCount.isNotEmpty() || postItemViewType is PostItemViewType.ThreadContextPostItem) {
                Box(
                    modifier = modifier
                        .background(color = PostColors.EmojiChipColors.background, CircleShape)
                        .clip(CircleShape)
                        .clickable(onClick = {
                            showEmojiDialog.value = true
                        })
                ) {
                    Icon(
                        modifier = Modifier
                            .size(emojiHeight)
                            .padding(5.dp),
                        imageVector = Icons.Default.InsertEmoticon,
                        contentDescription = null,
                    )
                }
            }
        }

        if (postItemViewType is PostItemViewType.ChatListItem) {
            val replyCount = postItemViewType.answerPosts.size

            if (replyCount > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        painter = painterResource(id = R.drawable.replies),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        style = MaterialTheme.typography.bodyMedium,
                        text = pluralStringResource(
                            id = R.plurals.communication_standalone_post_view_replies_button,
                            count = replyCount,
                            replyCount
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

private data class ReactionData(val reactionCount: Int, val hasClientReacted: Boolean)

@Composable
private fun AnimatedCounter(currentCount: Int, selected: Boolean) {
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
        Text(
            text = "$targetCount",
            fontSize = 12.sp,
            color = if (selected) MaterialTheme.colorScheme.primary else Color.Unspecified
        )
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
        if (selected) PostColors.EmojiChipColors.selectedBackgound else PostColors.EmojiChipColors.background

    Box(
        modifier = modifier
            .background(color = backgroundColor, shape)
            .clip(shape)
            .heightIn(max = emojiHeight)
            .clickable(onClick = onClick)
            .let {
                if (selected) {
                    it.border(1.dp, MaterialTheme.colorScheme.primary, shape)
                } else {
                    it
                }
            }
    ) {
        Row(
            modifier = Modifier
                .padding(2.dp)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = getUnicodeForEmojiId(emojiId = emojiId),
                fontSize = 12.sp
            )

            AnimatedCounter(reactionCount, selected)
        }
    }
}

@Composable
fun UndoDeleteHeader(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    delay: Long = 6000L
) {
    var remainingTime by remember { mutableLongStateOf(delay / 1000) }
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            animatedProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = delay.toInt(),
                    easing = LinearEasing
                )
            )
        }
        while (remainingTime > 0) {
            delay(1000L)
            remainingTime--
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = pluralStringResource(
                id = R.plurals.post_is_being_deleted,
                count = remainingTime.toInt(),
                remainingTime.toInt()
            ),
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.bodyMedium,
        )

        Box(
            modifier = Modifier
                .width(120.dp)
                .height(34.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                .clickable(onClick = onClick)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress.value)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.primary),
            )

            Text(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(4.dp),
                text = stringResource(id = R.string.post_undo_delete),
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

fun Modifier.applyGrayscale(isGrayscale: Boolean): Modifier {
    return if (isGrayscale) {
        this
            .drawWithCache {
                onDrawWithContent {
                    drawContent()
                    drawRect(Color.Black, blendMode = BlendMode.Saturation)
                }
            }
    } else {
        this
    }
}