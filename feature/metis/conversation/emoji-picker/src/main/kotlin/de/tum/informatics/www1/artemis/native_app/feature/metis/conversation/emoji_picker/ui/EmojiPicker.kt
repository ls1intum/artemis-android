package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicSearchTextField
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.content.Emoji
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.content.EmojiCategory
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.ui.extensions.header
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.ui.extensions.icon
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.ui.extensions.uiText
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
) {
    val emojiProvider = LocalEmojiServiceProvider.current
    val emojiCategories by emojiProvider.emojiCategoriesFlow.collectAsState(emptyList())
    if (emojiCategories.isEmpty()) {
        CircularProgressIndicator()
        return
    }

    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }

    val allEmojis by remember(emojiCategories) {
        derivedStateOf {
            emojiCategories
                .filter { it.id != EmojiCategory.Id.RECENT }
                .flatMap { it.emojis }
        }
    }
    val filteredEmojis = remember(allEmojis, searchQuery) {
        allEmojis.filter {
            it.emojiId.startsWith(searchQuery)
        }
    }

    Column(modifier = modifier) {
        BasicSearchTextField(
            hint = stringResource(R.string.emojis_search_hint),
            query = searchQuery,
            updateQuery = {
                searchQuery = it
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (searchQuery.isNotBlank()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {
                items(
                    count = filteredEmojis.size
                ) { index ->
                    EmojiLineItem(
                        modifier = Modifier.fillMaxWidth(),
                        emoji = filteredEmojis[index]
                    )
                }
            }
            return
        }

        EmojiOverview(
            emojiCategories = emojiCategories,
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
private fun EmojiOverview(
    modifier: Modifier = Modifier,
    emojiCategories: List<EmojiCategory>,
    onEmojiClicked: (Emoji) -> Unit
) {
    val gridState = rememberLazyGridState()

    Column(
        modifier = modifier
    ) {
        EmojiCategoryIconsRow(
            emojiCategories = emojiCategories,
            gridState = gridState,
        )

        HorizontalDivider()

        EmojiCategoryList(
            emojiCategories = emojiCategories,
            gridState = gridState,
            onEmojiClicked = onEmojiClicked
        )
    }
}


@Composable
private fun EmojiCategoryIconsRow(
    emojiCategories: List<EmojiCategory>,
    gridState: LazyGridState
) {
    val coroutineScope = rememberCoroutineScope()
    val headerIndices = remember(emojiCategories) {
        EmojiCategoryListUtil.getHeaderIndices(emojiCategories)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
    ) {
        emojiCategories.forEachIndexed { index, category ->
            val headerIndex = headerIndices[index]
            val nextHeaderIndex = if (index == emojiCategories.size - 1) Int.MAX_VALUE else headerIndices[index + 1]

            val isSelected = gridState.firstVisibleItemIndex in headerIndex..<nextHeaderIndex

            Icon(
                modifier = Modifier
                    .padding(2.dp)
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

@Composable
private fun EmojiItem(
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