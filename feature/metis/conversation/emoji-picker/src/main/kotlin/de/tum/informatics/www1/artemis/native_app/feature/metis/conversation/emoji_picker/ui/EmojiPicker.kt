package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.content.Emoji
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.content.EmojiCategory
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.ui.util.EmojiCategoryListUtil
import kotlinx.coroutines.launch

// Implementing our own custom emoji picker, because the one provided by emoji2 had two issues:
// - The picker was not scrollable when placed inside a ModalBottomSheet, and the issue persists for
//   almost 1.5 years now, see https://issuetracker.google.com/issues/301240745
// - In Artemis, we do not support emoji variations, like eg skin tone. This was not toggleable with
//   the emoji2 picker.
@Composable
fun EmojiPicker(
    modifier: Modifier = Modifier,
    onEmojiClicked: (Emoji) -> Unit,
    searchEnabled: Boolean = false,
) {
    val emojiProvider = LocalEmojiServiceProvider.current
    val emojiCategories by emojiProvider.emojiCategoriesFlow.collectAsState(emptyList())

    val coroutineScope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()

    Column(modifier = modifier) {
        EmojiCategoryIconsRow(
            emojiCategories = emojiCategories,
            gridState = gridState,
        )

        HorizontalDivider()

        EmojiCategoryList(
            emojiCategories = emojiCategories,
            gridState = gridState,
            onEmojiClicked = {
                coroutineScope.launch {
                    emojiProvider.storeRecentEmoji(it.emojiId)
                }

                onEmojiClicked(it)
            }
        )
    }
}


@Composable
private fun EmojiCategoryIconsRow(
    emojiCategories: List<EmojiCategory>,
    gridState: LazyGridState
) {
    val coroutineScope = rememberCoroutineScope()
    val headerIndices = EmojiCategoryListUtil.getHeaderIndices(emojiCategories)

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        emojiCategories.forEachIndexed { index, category ->
            val headerIndex = headerIndices[index]
            val nextHeaderIndex = if (index == emojiCategories.size - 1) Int.MAX_VALUE else headerIndices[index + 1]

            val isSelected = gridState.firstVisibleItemIndex in headerIndex..<nextHeaderIndex

            Icon(
                modifier = Modifier
                    .padding(4.dp)
                    .clip(CircleShape)
                    .clickable {
                        coroutineScope.launch {
                            gridState.animateScrollToItem(headerIndex)
                        }
                    }
                    .padding(8.dp),
                imageVector = category.id.icon,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                contentDescription = null,
            )
        }
    }
}


@Composable
private fun EmojiCategoryList(
    emojiCategories: List<EmojiCategory>,
    gridState: LazyGridState,
    onEmojiClicked: (Emoji) -> Unit
) {
    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        state = gridState,
        columns = GridCells.Adaptive(minSize = 32.dp)
    ) {
        emojiCategories.forEach { category ->
            header {
                EmojiCategoryHeader(
                    category = category
                )
            }

            if (category.id == EmojiCategory.Id.RECENT && category.emojis.isEmpty()) {
                header {
                    Text(
                        text = stringResource(R.string.no_recent_emojis)
                    )
                }
            }

            items(
                count = category.emojis.size,
            ) { index ->
                val emoji = category.emojis[index]
                EmojiItem(
                    emoji = emoji,
                    onClick = { onEmojiClicked(emoji) }
                )
            }
        }
    }
}

@Composable
private fun EmojiCategoryHeader(
    modifier: Modifier = Modifier,
    category: EmojiCategory
) {
    val text = category.id.uiText()

    Text(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        text = text.uppercase(),
        style = MaterialTheme.typography.titleSmall
    )
}

private fun LazyGridScope.EmojiGrid(
    modifier: Modifier = Modifier,
    emojis: List<Emoji>,
    emptyText: String? = null,
    onEmojiClicked: (Emoji) -> Unit
) {
    if (emojis.isEmpty()) {
        emptyText?.let {
            header {
                Text(
                    modifier = modifier,
                    text = it,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        return
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