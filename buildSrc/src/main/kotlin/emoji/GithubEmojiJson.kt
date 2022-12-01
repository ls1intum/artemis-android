package emoji
import kotlinx.serialization.Serializable

@Serializable
data class GithubEmojiJson(
    val emojis: Map<String, EmojiData>
) {
    @Serializable
    data class EmojiData(
        val id: String,
        val name: String,
        val skins: List<EmojiSkin>
    ) {
        @Serializable
        data class EmojiSkin(
            val native: String
        )
    }
}