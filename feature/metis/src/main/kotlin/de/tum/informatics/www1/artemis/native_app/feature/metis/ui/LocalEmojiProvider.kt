package de.tum.informatics.www1.artemis.native_app.feature.metis.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.EmojiService
import org.koin.androidx.compose.get
import org.koin.compose.koinInject

internal val LocalEmojiProvider: ProvidableCompositionLocal<EmojiProvider> =
    compositionLocalOf { throw RuntimeException("No emoji provider set. Please call ProvideEmojis as a parent of your composable.") }

internal data class EmojiProvider(
    val unicodeForEmojiIdMap: MutableState<Map<String, String>?>,
    val unicodeToEmojiIdMap: MutableState<Map<String, String>?>
)

/**
 * Loads emojis from the [EmojiService] and makes them available for the UI.
 * You cannot use the communication emojis outside of children of this composable.
 */
@Composable
fun ProvideEmojis(content: @Composable () -> Unit) {
    val emojiService: EmojiService = koinInject()

    val unicodeForEmojiIdMap: MutableState<Map<String, String>?> = remember { mutableStateOf(null) }
    val unicodeToEmojiIdMap: MutableState<Map<String, String>?> = remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        if (unicodeForEmojiIdMap.value == null) {
            val emojiToUnicodeMap = emojiService.getEmojiToUnicodeMap()
            unicodeForEmojiIdMap.value = emojiToUnicodeMap
            unicodeToEmojiIdMap.value = emojiToUnicodeMap.map { it.value to it.key }.toMap()
        }
    }

    CompositionLocalProvider(
        LocalEmojiProvider provides EmojiProvider(
            unicodeForEmojiIdMap = unicodeForEmojiIdMap,
            unicodeToEmojiIdMap = unicodeToEmojiIdMap
        ),
        content = content
    )
}

@Composable
fun getUnicodeForEmojiId(emojiId: String): String {
    val map by LocalEmojiProvider.current.unicodeForEmojiIdMap
    return remember(map, emojiId) {
        derivedStateOf { map?.get(emojiId) ?: "?" }
    }.value
}