package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.content.emoji

data class EmojiCategory(
    val id: Id,
    val emojis: List<Emoji>
) {

    enum class Id(val value: String) {
        PEOPLE("people"),
        NATURE("nature"),
        FOODS("foods"),
        ACTIVITY("activity"),
        PLACES("places"),
        OBJECTS("objects"),
        SYMBOLS("symbols"),
        FLAGS("flags");

        companion object {
            fun fromValue(value: String): Id = entries.first { it.value == value }
        }
    }
}