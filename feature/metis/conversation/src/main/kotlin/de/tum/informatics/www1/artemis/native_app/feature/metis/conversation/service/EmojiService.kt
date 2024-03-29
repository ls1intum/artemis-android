package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service

import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.content.emoji.EmojiCategory

interface EmojiService {
    suspend fun emojiIdToUnicode(emojiId: String): String

    suspend fun getEmojiToUnicodeMap(): Map<String, String>

    suspend fun getEmojiCategories(): List<EmojiCategory>
}