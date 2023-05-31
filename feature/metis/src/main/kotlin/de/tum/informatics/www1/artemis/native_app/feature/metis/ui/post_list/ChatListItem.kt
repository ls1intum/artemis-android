package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post_list

import de.tum.informatics.www1.artemis.native_app.feature.metis.model.Post
import kotlinx.datetime.LocalDate

sealed interface ChatListItem {
    data class PostChatListItem(val post: Post) : ChatListItem

    data class DateDivider(val localDate: LocalDate) : ChatListItem
}