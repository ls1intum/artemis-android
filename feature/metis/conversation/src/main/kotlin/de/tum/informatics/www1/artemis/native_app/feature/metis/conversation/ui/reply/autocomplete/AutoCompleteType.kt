package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.autocomplete

import androidx.annotation.StringRes
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R

enum class AutoCompleteType(@StringRes val title: Int) {
    USERS(R.string.markdown_textfield_autocomplete_category_users),
    CHANNELS(R.string.markdown_textfield_autocomplete_category_channels),
    LECTURES(R.string.markdown_textfield_autocomplete_category_lectures),
    EXERCISES(R.string.markdown_textfield_autocomplete_category_exercises),
    FAQS(R.string.markdown_textfield_autocomplete_category_faqs),
}