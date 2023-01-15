package emoji

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OutputEmojiJson(
    val categories: List<GithubEmojiJson.Category>,
    val entries: List<OutputEmojiEntry>
)

@Serializable
data class OutputEmojiEntry(
    @SerialName("a")
    val id: String,
    @SerialName("b")
    val unicode: String
)