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
import org.koin.androidx.compose.get

private val LocalEmojiProvider: ProvidableCompositionLocal<EmojiProvider> =
    compositionLocalOf { throw RuntimeException("No emoji provider set. Please call ProvideEmojis") }

private data class EmojiProvider(val unicodeForEmojiIdMap: MutableState<Map<String, String>?>)

@Composable
fun ProvideEmojis(content: @Composable () -> Unit) {
    val emojiService: de.tum.informatics.www1.artemis.native_app.feature.metis.emoji.EmojiService = get()

    val map: MutableState<Map<String, String>?> = remember { mutableStateOf(null) }

    LaunchedEffect(key1 = Unit) {
        if (map.value == null) {
            map.value = emojiService.getEmojiToUnicodeMap()
        }
    }

    CompositionLocalProvider(
        LocalEmojiProvider provides EmojiProvider(
            unicodeForEmojiIdMap = map
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