package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.content.emoji

import android.util.Log

data class EmojiCategory(
    val id: Id,
    val emojis: List<Emoji>
) {

    enum class Id(val value: String?) {
        RECENT(null),
        PEOPLE("people"),
        NATURE("nature"),
        FOODS("foods"),
        ACTIVITY("activity"),
        PLACES("places"),
        OBJECTS("objects"),
        SYMBOLS("symbols"),
        FLAGS("flags"),

        /** Special category to display in case of a unknown category (eg when a new category is added). */
        UNKNOWN(null);

        companion object {
            fun fromValue(value: String): Id {
                val id = entries.firstOrNull { it.value == value }
                if (id == null) {
                    Log.e("EmojiCategory", "Unknown category ID: $value")
                    return UNKNOWN
                }

                return id
            }
        }
    }
}