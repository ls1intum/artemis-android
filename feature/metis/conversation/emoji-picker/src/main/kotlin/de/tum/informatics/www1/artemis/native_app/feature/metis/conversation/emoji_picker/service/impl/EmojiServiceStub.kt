package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.service.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

object EmojiServiceStub :
    de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.service.EmojiService {
    override fun emojiIdToUnicode(emojiId: String): Flow<String> = emptyFlow()

    override val emojiCategoriesFlow: Flow<List<de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.content.EmojiCategory>> = emptyFlow()

    override suspend fun storeRecentEmoji(emojiId: String) {}
}
