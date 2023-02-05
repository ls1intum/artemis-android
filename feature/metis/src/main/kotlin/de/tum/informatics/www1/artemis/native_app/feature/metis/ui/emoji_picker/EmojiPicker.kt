package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.emoji_picker

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.RecyclerView
import de.tum.informatics.www1.artemis.native_app.feature.metis.emoji.EmojiCategory
import de.tum.informatics.www1.artemis.native_app.feature.metis.emoji.EmojiService
import org.koin.androidx.compose.get
import kotlin.math.roundToInt

/**
 * Display a list of emojis the user can choose from.
 */
@Composable
fun EmojiPicker(modifier: Modifier, onSelectEmoji: (emojiId: String) -> Unit) {
    val emojiService: EmojiService = get()

    var categories: List<EmojiCategory>? by remember { mutableStateOf(null) }

    var emojiToUnicodeMap: Map<String, String>? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        if (categories == null || emojiToUnicodeMap == null) {
            categories = emojiService.getEmojiCategories()
            emojiToUnicodeMap = emojiService.getEmojiToUnicodeMap()
        }
    }

    BoxWithConstraints(modifier = modifier) {
        val currentCategories = categories
        val currentMap = emojiToUnicodeMap

        if (currentCategories != null && currentMap != null) {
            EmojiPickerImpl(
                categories = currentCategories,
                emojiToUnicodeMap = currentMap,
                onSelectEmoji = onSelectEmoji
            )
        }
    }
}

@Composable
private fun BoxWithConstraintsScope.EmojiPickerImpl(
    categories: List<EmojiCategory>,
    emojiToUnicodeMap: Map<String, String>,
    onSelectEmoji: (emojiId: String) -> Unit
) {
    val context = LocalContext.current

    val emojiWidth = 40.dp
    val spacing = 8.dp

    val emojiPerRow = remember(maxWidth) {
        ((maxWidth - spacing) / (emojiWidth + spacing)).roundToInt()
    }

    val emojiFontSize = with(LocalDensity.current) {
        emojiWidth.toPx().toSp() / 2
    }

    val flattenedRecyclerViewItems: List<EmojiPickerViewItem> by remember(categories) {
        derivedStateOf {
            categories.flatMap { category ->
                listOf(EmojiPickerViewItem.CategoryHeader(category.id)) +
                        category.emojiIds.map { emojiId ->
                            EmojiPickerViewItem.Emoji(
                                emojiUnicode = emojiToUnicodeMap[emojiId] ?: "?",
                                emojiId = emojiId
                            )
                        }
            }
        }
    }

    val categoryTextColor = if (isSystemInDarkTheme()) Color.White else Color.Black

    val adapter by remember(
        flattenedRecyclerViewItems,
        emojiFontSize,
        categoryTextColor,
        onSelectEmoji
    ) {
        derivedStateOf {
            EmojiPickerAdapter(
                flattenedItems = flattenedRecyclerViewItems,
                emojiFontSizeInSp = emojiFontSize.value,
                categoryTextColor = categoryTextColor,
                onSelectEmoji = onSelectEmoji
            )
        }
    }

    val layoutManager = remember(flattenedRecyclerViewItems) {
        GridLayoutManager(context, emojiPerRow).apply {
            spanSizeLookup = object : SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (flattenedRecyclerViewItems[position]) {
                        is EmojiPickerViewItem.CategoryHeader -> emojiPerRow
                        is EmojiPickerViewItem.Emoji -> 1
                    }
                }
            }
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { viewContext ->
            RecyclerView(viewContext)
        },
        update = { recyclerView ->
            recyclerView.adapter = adapter
            recyclerView.layoutManager = layoutManager
        }
    )
}
