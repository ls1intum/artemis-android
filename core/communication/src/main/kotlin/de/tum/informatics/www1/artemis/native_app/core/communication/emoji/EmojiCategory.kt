package de.tum.informatics.www1.artemis.native_app.core.communication.emoji

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EmojiCategory(
    val id: String,
    @SerialName("emojis")
    val emojiIds: List<String>
)