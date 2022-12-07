package de.tum.informatics.www1.artemis.native_app.core.communication.ui.emoji_picker

import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import de.tum.informatics.www1.artemis.native_app.core.communication.R
import de.tum.informatics.www1.artemis.native_app.core.communication.emoji.EmojiService
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.getEmojiForEmojiId
import kotlinx.coroutines.flow.flow
import org.koin.androidx.compose.get
import kotlin.math.roundToInt

/**
 * Display a list of emojis the user can choose from.
 */
@Composable
fun EmojiPicker(modifier: Modifier, onSelectEmoji: (emojiId: String) -> Unit) {
    val emojiService: EmojiService = get()
    val categories = flow {
        emit(emojiService.getEmojiCategories())
    }.collectAsState(initial = emptyList()).value

    BoxWithConstraints(modifier = modifier) {
        val emojiWidth = 40.dp
        val spacing = 8.dp

        val emojiPerRow = remember(maxWidth) {
            ((maxWidth - spacing) / (emojiWidth + spacing)).roundToInt()
        }

        val emojiFontSize = with(LocalDensity.current) {
            emojiWidth.toPx().toSp().value / 2
        }

        val categoryEmojiChunks = remember(categories, emojiPerRow) {
            categories.associateWith { it.emojiIds.chunked(emojiPerRow) }
        }

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            categories.forEach { category ->
                item {
                    EmojiCategoryHeader(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        categoryId = category.id
                    )
                }

                items(categoryEmojiChunks[category].orEmpty()) { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing)
                    ) {
                        row.forEach { emojiId ->
                            val emojiUnicode = getEmojiForEmojiId(
                                emojiService = emojiService,
                                emojiId = emojiId
                            )

                            AndroidView(
                                modifier = Modifier
                                    .size(emojiWidth)
                                    .clickable(onClick = {
                                        onSelectEmoji(emojiId)
                                    }),
                                factory = { context ->
                                    AppCompatTextView(context).apply {
                                        setTextColor(Color.Black.toArgb())
                                        text = emojiUnicode
                                        textSize = emojiFontSize
                                        textAlignment = View.TEXT_ALIGNMENT_CENTER
                                    }
                                },
                                update = { textView ->
                                    textView.text = emojiUnicode
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmojiCategoryHeader(modifier: Modifier, categoryId: String) {
    val text = stringResource(
        id = when (categoryId) {
            "people" -> R.string.emoji_category_people
            "nature" -> R.string.emoji_category_nature
            "foods" -> R.string.emoji_category_foods
            "objects" -> R.string.emoji_category_objects
            "symbols" -> R.string.emoji_category_symbols
            "activity" -> R.string.emoji_category_activity
            "places" -> R.string.emoji_category_places
            "flags" -> R.string.emoji_category_flags
            else -> throw IllegalArgumentException("Unknown emoji category id = $categoryId")
        }
    )

    Text(
        modifier = modifier,
        text = text,
        style = MaterialTheme.typography.headlineSmall
    )
}