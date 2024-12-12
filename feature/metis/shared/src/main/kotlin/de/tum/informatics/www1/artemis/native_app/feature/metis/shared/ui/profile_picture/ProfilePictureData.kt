package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture

import androidx.compose.ui.graphics.Color
import de.tum.informatics.www1.artemis.native_app.core.model.account.BaseAccount
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.humanReadableName

sealed class ProfilePictureData {

    companion object {
        fun from(user: BaseAccount): ProfilePictureData = create(
            userId = user.id,
            username = user.humanReadableName,
            imageUrl = user.imageUrl
        )

        fun create(userId: Long?, username: String?, imageUrl: String?): ProfilePictureData {
            if (userId == null || username.isNullOrEmpty()) {
                return Unknown
            }

            val fallBack = InitialsPlaceholder(userId, username)
            return if (imageUrl != null) {
                Image(imageUrl, fallBack)
            } else {
                fallBack
            }
        }
    }

    data class Image(val url: String, val fallBack: InitialsPlaceholder) : ProfilePictureData()

    data class InitialsPlaceholder(val userId: Long, val username: String) : ProfilePictureData() {
            val initials: String = getInitialsFromString(username)
            val backgroundColor: Color = getBackgroundColorHue(userId.toString())
    }

    data object Unknown: ProfilePictureData()
}

// The following util functions are copied from the Artemis webapp implementation.
// Sources:
// https://github.com/ls1intum/Artemis/blob/fa32c243b568c92aa5e075e8176abdc7c7452444/src/main/webapp/app/utils/text.utils.ts
// https://github.com/ls1intum/Artemis/blob/fa32c243b568c92aa5e075e8176abdc7c7452444/src/main/webapp/app/utils/color.utils.ts

/**
 * Returns 2 capitalized initials of a given string.
 * If it has multiple names, it takes the first and last (Albert Berta Muster -> AM)
 * If it has one name, it'll return a deterministic random other string (Albert -> AB)
 * If it consists of a single letter it will return the single letter.
 * @param username The string used to generate the initials.
 */
private fun getInitialsFromString(username: String): String {
    val parts = username.trim().split("\\s+".toRegex())

    var initials: String

    if (parts.size > 1) {
        // Takes first and last word in string and returns their initials.
        initials = parts[0][0].toString() + parts[parts.size - 1][0]
    } else {
        // If only one single word, it will take the first letter and a random second.
        initials = parts[0][0].toString()
        val remainder = parts[0].substring(1)
        val secondInitial = remainder.find { it.isLetterOrDigit() }
        if (secondInitial != null) {
            initials += secondInitial
        }
    }

    return initials.uppercase()
}

/**
 * Returns a background color hue for a given string.
 * @param seed The string used to determine the random value.
 */
private fun getBackgroundColorHue(seed: String?): Color {
    val seedValue = seed ?: Math.random().toString()
    val hue = deterministicRandomValueFromString(seedValue) * 360
    return Color.hsl(hue.toFloat(), 0.5f, 0.5f)
}

/**
 * Returns a pseudo-random numeric value for a given string using a simple hash function.
 * @param str The string used for the hash function.
 */
private fun deterministicRandomValueFromString(str: String): Double {
    var seed = 0L
    for (i in str.indices) {
        seed = str[i].code + ((seed shl 5) - seed)
    }
    val m = 0x80000000
    val a = 1103515245
    val c = 42718

    seed = (a * seed + c) % m

    return seed.toDouble() / (m - 1)
}