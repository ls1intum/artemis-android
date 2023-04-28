package de.tum.informatics.www1.artemis.native_app.feature.metis.service

import de.tum.informatics.www1.artemis.native_app.feature.metis.emoji.EmojiCategory

interface EmojiService {
    suspend fun emojiIdToUnicode(emojiId: String): String

    suspend fun getEmojiToUnicodeMap(): Map<String, String>

    suspend fun getEmojiCategories(): List<EmojiCategory>
}