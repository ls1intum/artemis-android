package de.tum.informatics.www1.artemis.native_app.core.communication.ui

import androidx.compose.runtime.*
import de.tum.informatics.www1.artemis.native_app.core.communication.emoji.EmojiService
import kotlinx.coroutines.flow.flow
import org.koin.androidx.compose.get

private val LocalEmojiProvider: ProvidableCompositionLocal<EmojiProvider> =
    compositionLocalOf { throw RuntimeException("No emoji provider set. Please call ProvideEmojis") }

private data class EmojiProvider(val unicodeForEmojiIdMap: MutableState<Map<String, String>?>)

@Composable
fun ProvideEmojis(content: @Composable () -> Unit) {
    val emojiService: EmojiService = get()

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