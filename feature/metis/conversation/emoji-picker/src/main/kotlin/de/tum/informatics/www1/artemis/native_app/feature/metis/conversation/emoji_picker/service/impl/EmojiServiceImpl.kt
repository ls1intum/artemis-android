package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.service.impl

import android.content.Context
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.content.Emoji
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.content.EmojiCategory
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.service.EmojiService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.service.RecentEmojiService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.service.UNKNOWN_EMOJI_REPLACEMENT
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

/**
 * Loads the emojis from the generated file
 */
class EmojiServiceImpl(
    context: Context,
    private val recentEmojiService: RecentEmojiService,
) : EmojiService {

    @OptIn(ExperimentalSerializationApi::class, DelicateCoroutinesApi::class)
    private val input: Flow<Input> = flow<Input> {
        emit(
            withContext(Dispatchers.IO) {
                context.resources.openRawResource(R.raw.emojis).use { stream ->
                    Json.decodeFromStream(stream)
                }
            }
        )
    }
        .shareIn(GlobalScope, started = SharingStarted.Lazily, replay = 1)

    @OptIn(DelicateCoroutinesApi::class)
    private val entries: Flow<Map<String, String>> = input.map { input ->
        input.entries.associate { it.id to it.unicode }
    }
        .shareIn(GlobalScope, started = SharingStarted.Lazily, replay = 1)

    @OptIn(DelicateCoroutinesApi::class)
    private val defaultCategories: Flow<List<EmojiCategory>> = combine(
        input,
        entries
    ) { input, entries ->
        input.categories.map { categoryEntry ->
            EmojiCategory(
                id = EmojiCategory.Id.fromValue(
                    categoryEntry.id
                ),
                emojis = categoryEntry.emojiIds.map {
                    Emoji(
                        emojiId = it,
                        unicode = entries[it] ?: UNKNOWN_EMOJI_REPLACEMENT
                    )
                }
            )
        }
    }
        .shareIn(GlobalScope, started = SharingStarted.Lazily, replay = 1)

    private val recentEmojiCategory: Flow<EmojiCategory> = recentEmojiService.recentEmojiIdsFlow.map { recentEmojiIds ->
        EmojiCategory(
            id = EmojiCategory.Id.RECENT,
            emojis = recentEmojiIds.map {
                Emoji(
                    emojiId = it,
                    unicode = emojiIdToUnicode(it).first()
                )
            }
        )
    }


    override fun emojiIdToUnicode(emojiId: String): Flow<String> = entries.map { it[emojiId] ?: UNKNOWN_EMOJI_REPLACEMENT }

    override val emojiCategoriesFlow: Flow<List<EmojiCategory>> = combine(
        defaultCategories,
        recentEmojiCategory
    ) { defaultCategories, recentEmojiCategory ->
        listOf(recentEmojiCategory) + defaultCategories
    }

    override suspend fun storeRecentEmoji(emojiId: String) {
        recentEmojiService.addRecentEmojiId(emojiId)
    }

    @Serializable
    data class EmojiEntry(
        @SerialName("a")
        val id: String,
        @SerialName("b")
        val unicode: String
    )

    @Serializable
    data class EmojiCategoryEntry(
        val id: String,
        @SerialName("emojis")
        val emojiIds: List<String>
    )

    @Serializable
    data class Input(
        val categories: List<EmojiCategoryEntry>,
        val entries: List<EmojiEntry>
    )
}