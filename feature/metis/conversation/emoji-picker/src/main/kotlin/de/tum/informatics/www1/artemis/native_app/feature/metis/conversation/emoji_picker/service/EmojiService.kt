package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.service

import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.content.EmojiCategory
import kotlinx.coroutines.flow.Flow

const val UNKNOWN_EMOJI_REPLACEMENT = "?"

interface EmojiService {
    fun emojiIdToUnicode(emojiId: String): Flow<String>

    val emojiCategoriesFlow: Flow<List<EmojiCategory>>

    suspend fun storeRecentEmoji(emojiId: String)
}