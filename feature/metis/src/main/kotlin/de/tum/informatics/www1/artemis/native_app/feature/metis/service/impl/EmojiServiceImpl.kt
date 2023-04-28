package de.tum.informatics.www1.artemis.native_app.feature.metis.service.impl

import android.content.Context
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.emoji.EmojiCategory
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.EmojiService
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

/**
 * Loads the emojis from the generated file
 */
class EmojiServiceImpl(context: Context) : EmojiService {

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


    override suspend fun emojiIdToUnicode(emojiId: String): String {
        val map = entries.first()
        return map[emojiId] ?: "\uD83D\uDDBE"
    }

    override suspend fun getEmojiToUnicodeMap(): Map<String, String> = entries.first()

    override suspend fun getEmojiCategories(): List<EmojiCategory> {
        return input.first().categories
    }

    @Serializable
    data class EmojiEntry(
        @SerialName("a")
        val id: String,
        @SerialName("b")
        val unicode: String
    )

    @Serializable
    data class Input(
        val categories: List<EmojiCategory>,
        val entries: List<EmojiEntry>
    )
}