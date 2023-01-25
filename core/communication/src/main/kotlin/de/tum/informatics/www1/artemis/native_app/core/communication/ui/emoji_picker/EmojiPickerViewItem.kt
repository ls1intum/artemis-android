package de.tum.informatics.www1.artemis.native_app.core.communication.ui.emoji_picker

sealed interface EmojiPickerViewItem {
    data class CategoryHeader(val categoryId: String) : EmojiPickerViewItem

    data class Emoji(val emojiUnicode: String, val emojiId: String) : EmojiPickerViewItem
}