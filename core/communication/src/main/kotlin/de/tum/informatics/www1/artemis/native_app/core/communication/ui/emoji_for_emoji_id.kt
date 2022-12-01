package de.tum.informatics.www1.artemis.native_app.core.communication.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import de.tum.informatics.www1.artemis.native_app.core.communication.EmojiService
import kotlinx.coroutines.flow.flow
import org.koin.androidx.compose.get

@Composable
fun getEmojiForEmojiId(emojiId: String): String {
    val emojiService: EmojiService = get()

    return flow {
        emit(emojiService.emojiIdToUnicode(emojiId))
    }.collectAsState(initial = "\uD83D\uDDBE").value
}