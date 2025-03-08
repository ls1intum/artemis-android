package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.ui.util

import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.content.EmojiCategory

object EmojiCategoryListUtil {

    fun getHeaderIndices(emojiCategoryList: List<EmojiCategory>): List<Int> {
        val headerIndices = mutableListOf<Int>()
        var index = 0
        for (emojiCategory in emojiCategoryList) {
            headerIndices.add(index)
            index += emojiCategory.emojis.size + 1

            if (emojiCategory.id == EmojiCategory.Id.RECENT && emojiCategory.emojis.isEmpty()) {
                // For the "No recent emojis" message header
                index += 1
            }
        }
        return headerIndices
    }
}