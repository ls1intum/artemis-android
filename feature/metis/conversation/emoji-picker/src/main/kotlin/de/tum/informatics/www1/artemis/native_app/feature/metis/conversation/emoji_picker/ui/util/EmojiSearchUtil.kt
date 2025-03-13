package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.ui.util

import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.content.Emoji

object EmojiSearchUtil {

    fun filterAndSortEmojis(emojis: List<Emoji>, query: String): List<Emoji> {
        return emojis
            .asSequence()
            .map { EmojiSearchResult.fromQuery(it, query) }
            .filter { it.isMatching }
            .sortedByDescending { it.score }
            .sortedBy { it.firstMatchingKeywordIndex }
            .map { it.emoji }
            .toList()
    }


    private data class EmojiSearchResult(
        val emoji: Emoji,
        val firstMatchingKeywordIndex: Int,
        val score: Float
    ) {
        val isMatching: Boolean
            get() = firstMatchingKeywordIndex != -1

        companion object {
            fun fromQuery(emoji: Emoji, query: String): EmojiSearchResult {
                val emojiId = emoji.emojiId
                val keywords = emojiId.split("_")
                val firstMatchingKeywordIndex = keywords.indexOfFirst {
                    it.startsWith(query, ignoreCase = true)
                }
                val score = query.length.toFloat() / emojiId.length     // Matches where only a part of the keyword is matched should be ranked lower
                return EmojiSearchResult(emoji, firstMatchingKeywordIndex, score)
            }
        }
    }
}