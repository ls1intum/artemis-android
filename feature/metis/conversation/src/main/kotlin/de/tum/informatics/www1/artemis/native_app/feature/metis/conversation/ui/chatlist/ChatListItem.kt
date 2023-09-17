package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist

import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.Post
import kotlinx.datetime.LocalDate

sealed class ChatListItem {
    data class PostChatListItem(val post: Post) : ChatListItem()

    data class DateDivider(val localDate: LocalDate) : ChatListItem()
}