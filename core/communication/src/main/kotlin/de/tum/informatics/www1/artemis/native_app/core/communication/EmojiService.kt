package de.tum.informatics.www1.artemis.native_app.core.communication

interface EmojiService {

    suspend fun emojiIdToUnicode(emojiId: String): String
}