package de.tum.informatics.www1.artemis.native_app.core.communication.ui.emoji_picker

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import de.tum.informatics.www1.artemis.native_app.core.communication.R

class EmojiPickerAdapter(
    private val flattenedItems: List<EmojiPickerViewItem>,
    private val emojiFontSizeInSp: Float,
    private val categoryTextColor: Color,
    private val onSelectEmoji: (emojiId: String) -> Unit
) :
    RecyclerView.Adapter<ViewHolder>() {

    private companion object {
        private const val VIEW_TYPE_CATEGORY_HEADER = 0
        private const val VIEW_TYPE_EMOJI = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (flattenedItems[position]) {
            is EmojiPickerViewItem.CategoryHeader -> VIEW_TYPE_CATEGORY_HEADER
            is EmojiPickerViewItem.Emoji -> VIEW_TYPE_EMOJI
        }
    }

    override fun getItemCount(): Int = flattenedItems.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            VIEW_TYPE_CATEGORY_HEADER -> CategoryHeaderViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.category_header, parent, false)
            )
            VIEW_TYPE_EMOJI -> EmojiViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.emoji_view, parent, false)
            )
            else -> throw IllegalArgumentException("Unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (val item = flattenedItems[position]) {
            is EmojiPickerViewItem.CategoryHeader -> {
                holder as CategoryHeaderViewHolder

                val categoryTitle = holder.itemView.context.getString(
                    when (item.categoryId) {
                        "people" -> R.string.emoji_category_people
                        "nature" -> R.string.emoji_category_nature
                        "foods" -> R.string.emoji_category_foods
                        "objects" -> R.string.emoji_category_objects
                        "symbols" -> R.string.emoji_category_symbols
                        "activity" -> R.string.emoji_category_activity
                        "places" -> R.string.emoji_category_places
                        "flags" -> R.string.emoji_category_flags
                        else -> throw IllegalArgumentException("Unknown emoji category id = ${item.categoryId}")
                    }
                )

                holder.title.text = categoryTitle
                holder.title.setTextColor(categoryTextColor.toArgb())
            }
            is EmojiPickerViewItem.Emoji -> {
                holder as EmojiViewHolder
                holder.textView.text = item.emojiUnicode
                holder.textView.textSize = emojiFontSizeInSp

                holder.itemView.setOnClickListener { onSelectEmoji(item.emojiId) }
            }
        }
    }
}
