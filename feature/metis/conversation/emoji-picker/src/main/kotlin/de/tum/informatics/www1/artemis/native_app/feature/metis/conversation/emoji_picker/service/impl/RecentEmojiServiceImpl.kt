package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.service.impl

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class RecentEmojiServiceImpl(
    context: Context,
) : de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.service.RecentEmojiService {

    private val MAX_RECENT_EMOJI_COUNT = 50

    private val sharedPreferences = context.getSharedPreferences("recent_emojis", Context.MODE_PRIVATE)
    private val recentEmojiIdsKey = "recent_emoji_ids"

    /**
     * A flow that emits the list of recent emoji IDs sorted by their usage count in descending order.
     */
    override val recentEmojiIdsFlow: Flow<List<String>> = flow {
        val recentEmojiIds = getStoredRecentEmojiIds()
        val emojiUsageCount = recentEmojiIds.groupingBy { it }.eachCount()
        val sortedEmojiIds = emojiUsageCount.entries.sortedByDescending { it.value }.map { it.key }
        emit(sortedEmojiIds)
    }

    /**
     * Adds a recent emoji ID to the list of recent emojis.
     * If the list exceeds the maximum count, the oldest emoji ID is removed.
     *
     * @param emojiId The ID of the emoji to add.
     */
    override suspend fun addRecentEmojiId(emojiId: String) {
        withContext(Dispatchers.IO) {
            val recentEmojiIds = getStoredRecentEmojiIds()
            recentEmojiIds.add(0, emojiId)
            if (recentEmojiIds.size > MAX_RECENT_EMOJI_COUNT) {
                recentEmojiIds.removeLastOrNull()
            }
            sharedPreferences.edit().putString(recentEmojiIdsKey, Json.encodeToString(recentEmojiIds)).apply()
        }
    }

    private fun getStoredRecentEmojiIds(): MutableList<String> {
        return sharedPreferences.getString(recentEmojiIdsKey, "[]")?.let {
            Json.decodeFromString<MutableList<String>>(it)
        } ?: mutableListOf()
    }
}