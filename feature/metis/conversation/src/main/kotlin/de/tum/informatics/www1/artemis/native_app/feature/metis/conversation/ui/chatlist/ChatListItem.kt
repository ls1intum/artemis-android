package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist

import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IStandalonePost
import kotlinx.datetime.LocalDate

sealed class ChatListItem {

    fun getItemKey(): Any = when (this) {
        is DateDivider -> localDate.toEpochDays()
        is IndexedItem.Post -> post.key
        is IndexedItem.PostWithForwardedMessage -> post.key
        is UnreadIndicator -> "unread"
    }

    sealed class IndexedItem : ChatListItem() {
        abstract val post: IStandalonePost
        abstract val index: Long

        data class Post(override val post: IStandalonePost, override val index: Long = -1L) :
            IndexedItem()

        data class PostWithForwardedMessage(
            override val post: IStandalonePost,
            override val index: Long = -1L,
            val forwardedPosts: List<IBasePost>
        ) : IndexedItem()
    }

    data class DateDivider(val localDate: LocalDate) : ChatListItem()

    data object UnreadIndicator : ChatListItem()
}
