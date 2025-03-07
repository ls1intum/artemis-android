package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.AddReaction
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.ui.EmojiPickerModalBottomSheet
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.DisplayPriority
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IAnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IReaction
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IStandalonePost

internal const val TEST_TAG_POST_CONTEXT_BOTTOM_SHEET = "TEST_TAG_POST_CONTEXT_BOTTOM_SHEET"

internal val predefinedEmojiIds = listOf("joy", "+1", "heavy_plus_sign", "rocket")

@Composable
internal fun PostContextBottomSheet(
    post: IBasePost,
    clientId: Long,
    postActions: PostActions,
    onDismissRequest: () -> Unit
) {
    var displayAllEmojis by remember { mutableStateOf(false) }

    if (displayAllEmojis) {
        postActions.onClickReaction?.let { onClickReaction ->
            EmojiPickerModalBottomSheet(
                onDismiss = onDismissRequest,
                onEmojiClicked = {
                    onDismissRequest()
                    onClickReaction(it.emojiId, true)
                }
            )
        }
        return
    }

    ModalBottomSheet(
        modifier = Modifier
            .statusBarsPadding()
            .testTag(TEST_TAG_POST_CONTEXT_BOTTOM_SHEET),
        contentWindowInsets = { WindowInsets.statusBars },
        sheetState = rememberModalBottomSheetState(),
        onDismissRequest = onDismissRequest
    ) {
        val actionButtonModifier = Modifier.fillMaxWidth()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacings.BottomSheetContentPadding)
        ) {
            postActions.onClickReaction?.let { onClickReaction ->
                EmojiReactionBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    presentReactions = post.reactions.orEmpty(),
                    clientId = clientId,
                    onReactWithEmoji = { emojiId ->
                        onDismissRequest()
                        onClickReaction(emojiId, true)
                    },
                    onRequestViewMoreEmojis = {
                        displayAllEmojis = true
                    }
                )
            }

            if (postActions.canPerformAnyAction) {
                HorizontalDivider()
            }

            postActions.requestEditPost?.let {
                BottomSheetActionButton(
                    modifier = actionButtonModifier,
                    icon = Icons.Default.Edit,
                    text = stringResource(id = R.string.post_edit),
                    onClick = {
                        onDismissRequest()
                        it()
                    }
                )
            }

            postActions.requestDeletePost?.let {
                BottomSheetActionButton(
                    modifier = actionButtonModifier,
                    icon = Icons.Default.Delete,
                    text = stringResource(id = R.string.post_delete),
                    onClick = {
                        onDismissRequest()
                        it()
                    }
                )
            }

            BottomSheetActionButton(
                modifier = actionButtonModifier,
                icon = Icons.Default.ContentCopy,
                text = stringResource(id = R.string.post_copy_text),
                onClick = {
                    onDismissRequest()
                    postActions.onCopyText()
                }
            )

            if (postActions.onResolvePost != null && post is IAnswerPost) {
                BottomSheetActionButton(
                    modifier = actionButtonModifier,
                    icon = if (post.resolvesPost) Icons.Default.Clear else Icons.Default.Check,
                    text = if (post.resolvesPost) stringResource(id = R.string.post_does_not_resolve) else stringResource(
                        id = R.string.post_resolves
                    ),
                    onClick = {
                        onDismissRequest()
                        postActions.onResolvePost.invoke()
                    }
                )
            }

            if (postActions.onPinPost != null && post is IStandalonePost) {
                val isPinned = post.displayPriority == DisplayPriority.PINNED
                BottomSheetActionButton(
                    modifier = actionButtonModifier,
                    icon = if (isPinned) ImageVector.vectorResource(R.drawable.unpin) else ImageVector.vectorResource(
                        R.drawable.pin
                    ),
                    text = if (isPinned) stringResource(id = R.string.post_unpin) else stringResource(
                        id = R.string.post_pin
                    ),
                    onClick = {
                        onDismissRequest()
                        postActions.onPinPost.invoke()
                    }
                )
            }

            if (postActions.onSavePost != null) {
                val isSaved = post.isSaved == true
                BottomSheetActionButton(
                    modifier = actionButtonModifier,
                    icon = if (isSaved) Icons.Default.BookmarkRemove else Icons.Default.BookmarkAdd,
                    text = if (isSaved) stringResource(id = R.string.post_unsave) else stringResource(
                        id = R.string.post_save
                    ),
                    onClick = {
                        onDismissRequest()
                        postActions.onSavePost.invoke()
                    }
                )
            }

            postActions.onReplyInThread?.let {
                BottomSheetActionButton(
                    modifier = actionButtonModifier,
                    icon = Icons.AutoMirrored.Filled.Reply,
                    text = stringResource(id = R.string.post_reply),
                    onClick = {
                        onDismissRequest()
                        it()
                    }
                )
            }
        }
    }
}

@Composable
private fun EmojiReactionBar(
    modifier: Modifier,
    clientId: Long,
    presentReactions: List<IReaction>,
    onReactWithEmoji: (emojiId: String) -> Unit,
    onRequestViewMoreEmojis: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val buttonModifier = Modifier.size(40.dp)

        predefinedEmojiIds.fastForEach { emojiId ->
            val alreadyExists = remember(presentReactions, emojiId, clientId) {
                presentReactions.any { it.creatorId == clientId && it.emojiId == emojiId }
            }

            EmojiButton(
                modifier = buttonModifier,
                onClick = { onReactWithEmoji(emojiId) },
                disabled = alreadyExists
            ) {
                Text(
                    modifier = Modifier.padding(4.dp),
                    text = de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.ui.getUnicodeForEmojiId(
                        emojiId = emojiId
                    ),
                    color = if (alreadyExists) LocalContentColor.current.copy(alpha = 0.38f)
                    else LocalContentColor.current
                )
            }
        }

        EmojiButton(
            modifier = buttonModifier,
            onClick = onRequestViewMoreEmojis,
            disabled = false
        ) {
            Icon(
                imageVector = Icons.Default.AddReaction,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun EmojiButton(
    modifier: Modifier,
    disabled: Boolean,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color = MaterialTheme.colorScheme.secondaryContainer, shape = CircleShape)
            .let {
                if (!disabled) {
                    it.clickable(onClick = onClick)
                } else it
            },
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
fun BottomSheetActionButton(
    modifier: Modifier,
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    TextButton(
        modifier = modifier,
        onClick = onClick
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            modifier = Modifier.weight(1f),
            text = text
        )
    }
}