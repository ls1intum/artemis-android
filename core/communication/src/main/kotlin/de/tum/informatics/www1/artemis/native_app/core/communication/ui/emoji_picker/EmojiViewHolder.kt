package de.tum.informatics.www1.artemis.native_app.core.communication.ui.emoji_picker

import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import de.tum.informatics.www1.artemis.native_app.core.communication.R

class EmojiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val textView: AppCompatTextView = itemView.findViewById(R.id.emojiView)
}