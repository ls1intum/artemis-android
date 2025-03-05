package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.emoji

import androidx.appcompat.view.ContextThemeWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.ViewCompat
import androidx.emoji2.emojipicker.EmojiPickerView

@Composable
internal fun EmojiPicker(
    modifier: Modifier,
    onEmojiClicked: (emojiId: String) -> Unit
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
                    onEmojiClicked(emojiId)
                }
            }
        }
    )
}