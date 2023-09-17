package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist

import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.PostPojo
import kotlinx.datetime.LocalDate

sealed class ChatListItem {
    data class PostChatListItem(val post: PostPojo) : ChatListItem()

    data class DateDivider(val localDate: LocalDate) : ChatListItem()
}