package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.impl

import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.content.emoji.EmojiCategory
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.EmojiService

object EmojiServiceStub : EmojiService {
    override suspend fun emojiIdToUnicode(emojiId: String): String = emojiId

    override suspend fun getEmojiToUnicodeMap(): Map<String, String> = emptyMap()

    override suspend fun getEmojiCategories(): List<EmojiCategory> = listOf()
}
