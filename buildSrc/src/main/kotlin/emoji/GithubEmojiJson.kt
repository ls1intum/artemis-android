package emoji
import kotlinx.serialization.Serializable

@Serializable
data class GithubEmojiJson(
    val categories: List<Category>,
    val emojis: Map<String, EmojiData>
) {
    @Serializable
    data class Category(val id: String, val emojis: List<String>)

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