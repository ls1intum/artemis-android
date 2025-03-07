package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalInspectionMode
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.service.EmojiService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.service.impl.EmojiServiceStub
import org.koin.compose.koinInject

internal val LocalEmojiServiceProvider: ProvidableCompositionLocal<EmojiService> =
    compositionLocalOf { throw RuntimeException("No emoji provider set. Please call ProvideEmojis as a parent of your composable.") }

@Composable
fun ProvideEmojis(
    emojiService: EmojiService = if (LocalInspectionMode.current) EmojiServiceStub else koinInject(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalEmojiServiceProvider provides emojiService,
        content = content
    )
}

@Composable
fun getUnicodeForEmojiId(emojiId: String): String {
    val unicode by LocalEmojiServiceProvider.current.emojiIdToUnicode(emojiId).collectAsState("")
    return unicode
}