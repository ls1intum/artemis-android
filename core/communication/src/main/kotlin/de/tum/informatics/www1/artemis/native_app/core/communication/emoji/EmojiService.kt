package de.tum.informatics.www1.artemis.native_app.core.communication.emoji

interface EmojiService {
    suspend fun emojiIdToUnicode(emojiId: String): String

    suspend fun getEmojiCategories(): List<EmojiCategory>
}