package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post

import androidx.appcompat.view.ContextThemeWrapper
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollDispatcher
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.ViewCompat
import androidx.emoji2.emojipicker.EmojiPickerView
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.LocalEmojiProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.getUnicodeForEmojiId

private val predefinedEmojiIds = listOf("heavy_plus_sign", "rocket", "tada", "recycle")
private val EmojiPickerHeight = 400.dp

@Composable
internal fun PostContextBottomSheet(
    postActions: PostActions,
    onDismissRequest: () -> Unit
) {
    var displayAllEmojis by remember { mutableStateOf(false) }

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
                onReactWithEmoji = postActions.onRequestReactWithEmoji,
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
}

@Composable
private fun EmojiReactionBar(
    modifier: Modifier,
    onReactWithEmoji: (emojiId: String) -> Unit,
    onRequestViewMoreEmojis: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val buttonModifier = Modifier.size(40.dp)

        predefinedEmojiIds.fastForEach { emojiId ->
            EmojiButton(
                modifier = buttonModifier,
                onClick = { onReactWithEmoji(emojiId) }
            ) {
                Text(
                    modifier = Modifier.padding(4.dp),
                    text = getUnicodeForEmojiId(emojiId = emojiId)
                )
            }
        }

        EmojiButton(
            modifier = buttonModifier,
            onClick = onRequestViewMoreEmojis
        ) {
            Icon(imageVector = Icons.Default.MoreHoriz, contentDescription = null)
        }
    }
}

@Composable
private fun EmojiButton(
    modifier: Modifier,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color = MaterialTheme.colorScheme.secondaryContainer, shape = CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
        content = content
    )
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