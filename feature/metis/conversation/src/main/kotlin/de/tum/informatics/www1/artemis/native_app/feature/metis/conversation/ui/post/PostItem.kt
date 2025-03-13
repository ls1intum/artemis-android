package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.InsertEmoticon
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.material.colors.PostColors
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.CreatePostService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.CreatePostStatus
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.model.LinkPreview
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.ui.post.PostItemMainContent
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.getUnicodeForEmojiId
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions.EmojiDialog
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions.EmojiSelection
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions.PostActions
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions.getTestTagForEmojiId
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.DisplayPriority
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IAnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IReaction
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IStandalonePost
import org.koin.compose.koinInject

sealed class PostItemViewType {

    data class ChatListItem(
        val answerPosts: List<IAnswerPost>
    ) : PostItemViewType()

    data object ThreadContextPostItem : PostItemViewType()

    data object ThreadAnswerItem : PostItemViewType()
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
    displayHeader: Boolean,
    postItemViewJoinedType: PostItemViewJoinedType,
    isMarkedAsDeleteList: SnapshotStateList<IBasePost>,
    postActions: PostActions,
    linkPreviews: List<LinkPreview>,
    onRemoveLinkPreview: (LinkPreview) -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRequestRetrySend: () -> Unit,
    onShowReactionsBottomSheet: (EmojiSelection) -> Unit
) {
    val isPlaceholder = post == null
    val isExpanded = when (postItemViewType) {
        PostItemViewType.ThreadContextPostItem -> true
        else -> false
    }
    val isDeleting by remember(post) { derivedStateOf { isMarkedAsDeleteList.contains(post) } }

    val isPinned = post is IStandalonePost && post.displayPriority == DisplayPriority.PINNED
    val isSaved = post?.isSaved == true
    val hasFooter = (post is IStandalonePost && post.answers.orEmpty()
        .isNotEmpty()) || post?.reactions.orEmpty().isNotEmpty() || isExpanded

    // Retrieve post status
    val clientPostId = post?.clientPostId
    val postStatus = when {
        post == null || post.serverPostId != null || clientPostId == null -> CreatePostStatus.FINISHED
        else -> {
            val createPostService: CreatePostService = koinInject()
            createPostService.observeCreatePostWorkStatus(clientPostId)
                .collectAsState(initial = CreatePostStatus.PENDING)
                .value
        }
    }

    PostItemMainContent(
        modifier = modifier
            .let {
                if (postStatus == CreatePostStatus.FAILED) {
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
            .padding(horizontal = Spacings.Post.innerSpacing),
        post = post,
        isExpanded = isExpanded,
        isAuthor = post?.authorId == clientId,
        isPlaceholder = isPlaceholder,
        isDeleting = isDeleting,
        postStatus = postStatus,
        displayHeader = displayHeader,
        linkPreviews = linkPreviews,
        onRemoveLinkPreview = onRemoveLinkPreview,
        onClick = onClick,
        onLongClick = onLongClick,
        onUndoDelete = { postActions.requestUndoDeletePost?.invoke() },
        leadingContent = {
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

            if (isSaved) {
                IconLabel(
                    modifier = applyDistancePaddingToModifier(Modifier)
                        .fillMaxWidth(),
                    resourceString = R.string.post_is_saved,
                    icon = Icons.Outlined.Bookmark
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
        },
        trailingContent = {
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
                postActions = postActions,
                onShowReactionsBottomSheet = onShowReactionsBottomSheet
            )
        }
    )
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

/**
 * Display the tags, the reactions and the action buttons like reply, view replies and react with emoji.
 */
@Composable
private fun StandalonePostFooter(
    modifier: Modifier,
    clientId: Long,
    reactions: List<IReaction>,
    postItemViewType: PostItemViewType,
    postActions: PostActions,
    onShowReactionsBottomSheet: (EmojiSelection) -> Unit
) {
    val reactionCount: Map<String, ReactionData> = remember(reactions, clientId) {
        reactions.groupBy { it.emojiId }.mapValues { groupedReactions ->
            ReactionData(
                groupedReactions.value.size,
                groupedReactions.value.any { it.creatorId == clientId }
            )
        }
    }
    var showEmojiDialog by remember { mutableStateOf(false) }

    if (showEmojiDialog) {
        EmojiDialog(
            onDismissRequest = { showEmojiDialog = false },
            onSelectEmoji = { emojiId ->
                postActions.onClickReaction?.invoke(emojiId, true)
                showEmojiDialog = false
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
                    },
                    onLongClick = onShowReactionsBottomSheet
                )
            }
            if (reactionCount.isNotEmpty() || postItemViewType is PostItemViewType.ThreadContextPostItem) {
                Box(
                    modifier = modifier
                        .background(color = PostColors.EmojiChipColors.background, CircleShape)
                        .clip(CircleShape)
                        .sizeIn(minHeight = Spacings.Post.emojiHeight, minWidth = Spacings.Post.emojiHeight)
                        .padding(with(LocalDensity.current) { 5.sp.toDp() } )
                        .clickable(onClick = {
                            showEmojiDialog = true
                        })
                ) {
                    Icon(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(with(LocalDensity.current) { Spacings.Post.addEmojiIconSize.toDp() } ),
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
            fontSize = Spacings.Post.emojiTextSize,
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
    onClick: () -> Unit,
    onLongClick: (EmojiSelection) -> Unit
) {
    val shape = CircleShape

    val backgroundColor =
        if (selected) PostColors.EmojiChipColors.selectedBackgound else PostColors.EmojiChipColors.background

    Box(
        modifier = modifier
            .background(color = backgroundColor, shape)
            .clip(shape)
            .heightIn(min = Spacings.Post.emojiHeight)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { onLongClick(EmojiSelection.SINGLE(emojiId)) }
            )
            .let {
                if (selected) {
                    it.border(1.dp, MaterialTheme.colorScheme.primary, shape)
                } else {
                    it
                }
            }
            .testTag(getTestTagForEmojiId(emojiId, "POST_ITEM"))
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 6.dp, vertical = 1.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = getUnicodeForEmojiId(emojiId = emojiId),
                fontSize = Spacings.Post.emojiTextSize
            )

            AnimatedCounter(reactionCount, selected)
        }
    }
}