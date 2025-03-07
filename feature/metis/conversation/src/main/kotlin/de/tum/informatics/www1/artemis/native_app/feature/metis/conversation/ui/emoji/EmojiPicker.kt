package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.emoji

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
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
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.content.emoji.Emoji
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.content.emoji.EmojiCategory
import kotlinx.coroutines.launch

// Implementing our own custom emoji picker, because the one provided by emoji2 had two issues:
// - The picker was not scrollable when placed inside a ModalBottomSheet, and the issue persists for
//   almost 1.5 years now, see https://issuetracker.google.com/issues/301240745
// - In Artemis, we do not support emoji variations, like eg skin tone. This was not toggleable with
//   the emoji2 picker.
@Composable
internal fun EmojiPicker(
    modifier: Modifier = Modifier,
    onEmojiClicked: (Emoji) -> Unit,
    searchEnabled: Boolean = false,
) {
    val emojiProvider = LocalEmojiServiceProvider.current
    val emojiCategories by emojiProvider.emojiCategoriesFlow.collectAsState(emptyList())

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    Column(modifier = modifier) {
        EmojiCategoryIconsRow(
            emojiCategories = emojiCategories,
            listState = listState
        )

        HorizontalDivider()

        EmojiCategoryList(
            emojiCategories = emojiCategories,
            listState = listState,
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
    listState: LazyListState
) {
    val coroutineScope = rememberCoroutineScope()
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        emojiCategories.forEachIndexed { index, category ->
            val headerIndex = index * 2
            val emojiListIndex = headerIndex + 1

            val isSelected = listState.firstVisibleItemIndex == headerIndex ||
                    listState.firstVisibleItemIndex == emojiListIndex
            Icon(
                modifier = Modifier
                    .padding(4.dp)
                    .clip(CircleShape)
                    .clickable {
                        coroutineScope.launch {
                            listState.animateScrollToItem(headerIndex)
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
    listState: LazyListState,
    onEmojiClicked: (Emoji) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        state = listState,
//        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        emojiCategories.forEach { category ->
            item {
                EmojiCategoryHeader(
                    category = category
                )
            }
            item {
                EmojiGrid(
                    emojis = category.emojis,
                    emptyText = if (category.id == EmojiCategory.Id.RECENT) stringResource(R.string.no_recent_emojis) else null,
                    onEmojiClicked = onEmojiClicked
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

@Composable
private fun EmojiGrid(
    modifier: Modifier = Modifier,
    emojis: List<Emoji>,
    emptyText: String? = null,
    onEmojiClicked: (Emoji) -> Unit
) {
    if (emojis.isEmpty()) {
        emptyText?.let {
            Text(
                modifier = modifier,
                text = it,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        return
    }

    FlowRow(
        modifier = modifier,
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