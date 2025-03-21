package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.service

import kotlinx.coroutines.flow.Flow

interface RecentEmojiService {

    val recentEmojiIdsFlow: Flow<List<String>>

    suspend fun addRecentEmojiId(emojiId: String)
}