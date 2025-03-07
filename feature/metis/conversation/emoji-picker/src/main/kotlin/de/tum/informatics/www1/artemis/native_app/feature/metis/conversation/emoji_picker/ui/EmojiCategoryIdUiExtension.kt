package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.EmojiFlags
import androidx.compose.material.icons.filled.EmojiFoodBeverage
import androidx.compose.material.icons.filled.EmojiNature
import androidx.compose.material.icons.filled.EmojiObjects
import androidx.compose.material.icons.filled.EmojiSymbols
import androidx.compose.material.icons.filled.EmojiTransportation
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.content.EmojiCategory

@Composable
fun EmojiCategory.Id.uiText(): String {
    val resId = when (this) {
        EmojiCategory.Id.RECENT -> R.string.emoji_category_recent
        EmojiCategory.Id.PEOPLE -> R.string.emoji_category_people
        EmojiCategory.Id.NATURE -> R.string.emoji_category_nature
        EmojiCategory.Id.FOODS -> R.string.emoji_category_foods
        EmojiCategory.Id.ACTIVITY -> R.string.emoji_category_activity
        EmojiCategory.Id.PLACES -> R.string.emoji_category_places
        EmojiCategory.Id.OBJECTS -> R.string.emoji_category_objects
        EmojiCategory.Id.SYMBOLS -> R.string.emoji_category_symbols
        EmojiCategory.Id.FLAGS -> R.string.emoji_category_flags
        EmojiCategory.Id.UNKNOWN -> return ""
    }

    return stringResource(resId)
}

val EmojiCategory.Id.icon: ImageVector
    get() = when (this) {
        EmojiCategory.Id.RECENT -> Icons.Default.AccessTime
        EmojiCategory.Id.PEOPLE -> Icons.Default.EmojiEmotions
        EmojiCategory.Id.NATURE -> Icons.Default.EmojiNature
        EmojiCategory.Id.FOODS -> Icons.Default.EmojiFoodBeverage
        EmojiCategory.Id.ACTIVITY -> Icons.Default.EmojiEvents
        EmojiCategory.Id.PLACES -> Icons.Default.EmojiTransportation
        EmojiCategory.Id.OBJECTS -> Icons.Default.EmojiObjects
        EmojiCategory.Id.SYMBOLS -> Icons.Default.EmojiSymbols
        EmojiCategory.Id.FLAGS -> Icons.Default.EmojiFlags
        EmojiCategory.Id.UNKNOWN -> Icons.Default.QuestionMark
    }