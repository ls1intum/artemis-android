package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.ui.util

import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.content.Emoji

object EmojiSearchUtil {

    fun filterAndSortEmojis(emojis: List<Emoji>, query: String): List<Emoji> {
        return emojis
            .asSequence()
            .map { EmojiSearchResult.fromQuery(it, query) }
            .filterIsInstance<EmojiSearchResult.Match>()
            .sortedBy { it.score }
            .map { it.emoji }
            .toList()
    }

    private sealed class EmojiSearchResult {

        companion object {
            fun fromQuery(emoji: Emoji, query: String): EmojiSearchResult {
                val firstMatchingKeywordIndex = emoji.keywords.indexOfFirst {
                    it.startsWith(query, ignoreCase = true)
                }

                if (firstMatchingKeywordIndex == -1) {
                    return NoMatch
                }

                val matchPortion = query.length.toFloat() / emoji.emojiId.length        // Between 0 and 1
                return Match(
                    emoji = emoji,
                    score = firstMatchingKeywordIndex - matchPortion
                )
            }
        }

        data object NoMatch: EmojiSearchResult()

        data class Match(
            val emoji: Emoji,
            /** Lower is better */
            val score: Float
        ) : EmojiSearchResult()
    }
}