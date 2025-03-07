package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.impl

import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.content.emoji.EmojiCategory
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.EmojiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

object EmojiServiceStub : EmojiService {
    override fun emojiIdToUnicode(emojiId: String): Flow<String> = emptyFlow()

    override val emojiCategoriesFlow: Flow<List<EmojiCategory>> = emptyFlow()

    override suspend fun storeRecentEmoji(emojiId: String) {}
}
