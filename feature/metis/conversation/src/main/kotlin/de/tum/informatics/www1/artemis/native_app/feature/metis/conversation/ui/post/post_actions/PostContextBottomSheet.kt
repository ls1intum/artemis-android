package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions

import androidx.appcompat.view.ContextThemeWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.AddReaction
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.view.ViewCompat
import androidx.emoji2.emojipicker.EmojiPickerView
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.LocalEmojiProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.getUnicodeForEmojiId
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.DisplayPriority
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IAnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IReaction
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IStandalonePost

internal const val TEST_TAG_POST_CONTEXT_BOTTOM_SHEET = "TEST_TAG_POST_CONTEXT_BOTTOM_SHEET"

internal val predefinedEmojiIds = listOf("heavy_plus_sign", "rocket", "tada", "recycle")

@Composable
internal fun PostContextBottomSheet(
    post: IBasePost,
    clientId: Long,
    postActions: PostActions,
    onDismissRequest: () -> Unit
) {
    var displayAllEmojis by remember { mutableStateOf(false) }

    if (!displayAllEmojis) {
        ModalBottomSheet(
            modifier = Modifier.testTag(TEST_TAG_POST_CONTEXT_BOTTOM_SHEET),
            contentWindowInsets = { WindowInsets.statusBars },
            sheetState = rememberModalBottomSheetState(),
            onDismissRequest = onDismissRequest
        ) {
            val actionButtonModifier = Modifier.fillMaxWidth()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacings.ScreenHorizontalSpacing)
                    .padding(bottom = 40.dp)
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
                    ActionButton(
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
                    ActionButton(
                        modifier = actionButtonModifier,
                        icon = Icons.Default.Delete,
                        text = stringResource(id = R.string.post_delete),
                        onClick = {
                            onDismissRequest()
                            it()
                        }
                    )
                }

                ActionButton(
                    modifier = actionButtonModifier,
                    icon = Icons.Default.ContentCopy,
                    text = stringResource(id = R.string.post_copy_text),
                    onClick = {
                        onDismissRequest()
                        postActions.onCopyText()
                    }
                )

                if (postActions.onResolvePost != null && post is IAnswerPost) {
                    ActionButton(
                        modifier = actionButtonModifier,
                        icon = if (post.resolvesPost) Icons.Default.Clear else Icons.Default.Check,
                        text = if (post.resolvesPost) stringResource(id = R.string.post_does_not_resolve) else stringResource(id = R.string.post_resolves),
                        onClick = {
                            onDismissRequest()
                            postActions.onResolvePost.invoke()
                        }
                    )
                }

                if (postActions.onPinPost != null && post is IStandalonePost) {
                    ActionButton(
                        modifier = actionButtonModifier,
                        icon = if (post.displayPriority == DisplayPriority.PINNED) ImageVector.vectorResource(R.drawable.unpin) else ImageVector.vectorResource(R.drawable.pin),
                        text = if (post.displayPriority == DisplayPriority.PINNED) stringResource(id = R.string.post_unpin) else stringResource(id = R.string.post_pin),
                        onClick = {
                            onDismissRequest()
                            postActions.onPinPost.invoke()
                        }
                    )
                }

                postActions.onReplyInThread?.let {
                    ActionButton(
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
    } else {
        postActions.onClickReaction?.let { onClickReaction ->
            EmojiDialog(
                onDismissRequest = onDismissRequest,
                onSelectEmoji = { emojiId ->
                    onDismissRequest()
                    onClickReaction(emojiId, true)
                }
            )
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
                    text = getUnicodeForEmojiId(emojiId = emojiId),
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
private fun ActionButton(
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

        Box(modifier = Modifier.width(16.dp))

        Text(
            modifier = Modifier.weight(1f),
            text = text
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
                .clip(MaterialTheme.shapes.large)
        ) {
            EmojiPicker(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
                    .padding(horizontal = 8.dp),
                onReactWithEmoji = onSelectEmoji
            )
        }
    }
}

@Composable
private fun EmojiPicker(
    modifier: Modifier,
    onReactWithEmoji: (emojiId: String) -> Unit
) {
    val emojiProvider = LocalEmojiProvider.current
    val unicodeToEmojiIdMap by emojiProvider.unicodeToEmojiIdMap

    AndroidView(
        modifier = modifier,
        factory = { context ->
            EmojiPickerView(
                ContextThemeWrapper(
                    context,
                    androidx.appcompat.R.style.Theme_AppCompat_DayNight
                )
            ).apply {
                ViewCompat.setNestedScrollingEnabled(this, true)
            }
        },
        update = { emojiPicker ->
            emojiPicker.setOnEmojiPickedListener { pickedEmoji ->
                val emojiId = unicodeToEmojiIdMap?.get(pickedEmoji.emoji)
                if (emojiId != null) {
                    onReactWithEmoji(emojiId)
                }
            }
        }
    )
}