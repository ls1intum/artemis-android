package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist

import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IStandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.PostPojo
import kotlinx.datetime.LocalDate

sealed class ChatListItem {

    fun getItemKey(): Any = when (this) {
        is DateDivider -> localDate.toEpochDays()
        is PostChatListItem -> post.key
    }

    data class PostChatListItem(val post: IStandalonePost) : ChatListItem()

    data class DateDivider(val localDate: LocalDate) : ChatListItem()
}
