package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post

import androidx.appcompat.view.ContextThemeWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Divider
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.view.ViewCompat
import androidx.emoji2.emojipicker.EmojiPickerView
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.IReaction
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.LocalEmojiProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.getUnicodeForEmojiId

private val predefinedEmojiIds = listOf("heavy_plus_sign", "rocket", "tada", "recycle")

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
            modifier = Modifier,
            sheetState = rememberModalBottomSheetState(),
            onDismissRequest = onDismissRequest
        ) {
            val actionButtonModifier = Modifier.fillMaxWidth()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacings.ScreenHorizontalSpacing)
            ) {
                EmojiReactionBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    presentReactions = post.reactions.orEmpty(),
                    clientId = clientId,
                    onReactWithEmoji = {
                        onDismissRequest()
                        postActions.onClickReaction(it, true)
                    },
                    onRequestViewMoreEmojis = {
                        displayAllEmojis = true
                    }
                )

                if (postActions.canPerformAnyAction) {
                    Divider()
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
            }
        }
    } else {
        EmojiDialog(
            onDismissRequest = onDismissRequest,
            onSelectEmoji = {
                onDismissRequest()
                postActions.onClickReaction(it, true)
            }
        )
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
                imageVector = Icons.Default.MoreHoriz,
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
                .clip(RoundedCornerShape(5))
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