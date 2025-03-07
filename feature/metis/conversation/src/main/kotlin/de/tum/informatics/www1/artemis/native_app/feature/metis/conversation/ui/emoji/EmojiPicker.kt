package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.emoji

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.content.emoji.Emoji
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.content.emoji.EmojiCategory

@Composable
internal fun EmojiPicker(
    modifier: Modifier = Modifier,
    onEmojiClicked: (emojiId: String) -> Unit,
    searchEnabled: Boolean = false,
) {
    val emojiProvider = LocalEmojiProvider.current
    val emojiCategories by emojiProvider.emojiCategories

    Column(modifier = modifier) {

        EmojiCategoryList(
            emojiCategories = emojiCategories ?: emptyList(),
            onEmojiClicked = {
                onEmojiClicked(it.emojiId)
            }
        )
    }
}

@Composable
fun EmojiCategoryList(
    emojiCategories: List<EmojiCategory>,
    onEmojiClicked: (Emoji) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
    ) {
        emojiCategories.forEach { category ->
            item {
                EmojiCategoryHeader(
                    categoryId = category.id
                )
            }
            item {
                EmojiGrid(
                    emojis = category.emojis,
                    onEmojiClicked = onEmojiClicked
                )
            }
        }
    }
}

@Composable
private fun EmojiCategoryHeader(
    modifier: Modifier = Modifier,
    categoryId: EmojiCategory.Id
) {
    val stringRes = when (categoryId) {
        EmojiCategory.Id.PEOPLE -> R.string.emoji_category_people
        EmojiCategory.Id.NATURE -> R.string.emoji_category_nature
        EmojiCategory.Id.FOODS -> R.string.emoji_category_foods
        EmojiCategory.Id.ACTIVITY -> R.string.emoji_category_activity
        EmojiCategory.Id.PLACES -> R.string.emoji_category_places
        EmojiCategory.Id.OBJECTS -> R.string.emoji_category_objects
        EmojiCategory.Id.SYMBOLS -> R.string.emoji_category_symbols
        EmojiCategory.Id.FLAGS -> R.string.emoji_category_flags
    }
    val text = stringResource(id = stringRes)

    Text(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        text = text.uppercase(),
        style = MaterialTheme.typography.titleSmall
    )
}

@Composable
private fun EmojiGrid(
    modifier: Modifier = Modifier,
    emojis: List<Emoji>,
    onEmojiClicked: (Emoji) -> Unit
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        emojis.forEach { emoji ->
            EmojiItem(
                emoji = emoji,
                onClick = { onEmojiClicked(emoji) }
            )
        }
    }
}

@Composable
fun EmojiItem(
    emoji: Emoji,
    onClick: () -> Unit
) {
    Text(
        modifier = Modifier
            .padding(2.dp)
            .clickable { onClick() },
        text = emoji.unicode,
        style = MaterialTheme.typography.headlineMedium
    )
}