package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.emoji.emoji_picker

import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import de.tum.informatics.www1.artemis.native_app.feature.metis.R

class CategoryHeaderViewHolder(itemView: View) : ViewHolder(itemView) {
    val title: AppCompatTextView = itemView.findViewById(R.id.categoryHeader)
}