package emoji

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class OutputEmojiEntry(
    @SerialName("a")
    val id: String,
    @SerialName("b")
    val unicode: String
)