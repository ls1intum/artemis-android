package de.tum.informatics.www1.artemis.native_app.core.communication.ui.emoji_picker

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import de.tum.informatics.www1.artemis.native_app.core.communication.R

class CategoryHeaderViewHolder(itemView: View) : ViewHolder(itemView) {
    val title: AppCompatTextView = itemView.findViewById(R.id.categoryHeader)
}