package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist

import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IAnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IStandalonePost
import kotlinx.datetime.LocalDate

sealed class ChatListItem {

    fun getItemKey(): Any = when (this) {
        is DateDivider -> localDate.toEpochDays()
        is PostItem -> key
        is UnreadIndicator -> "unread"
    }

    sealed class PostItem : ChatListItem() {
        abstract val post: IBasePost
        abstract val index: Long
        val key: Any
            get() = post.key

        interface ForwardedMessage {
            val forwardedPosts: List<IBasePost>
        }

        sealed class IndexedItem : PostItem() {
            abstract val answers: List<IAnswerPost>

            data class Post(
                override val post: IStandalonePost,
                override val answers: List<IAnswerPost>,
                override val index: Long = -1L
            ) : IndexedItem()

            data class PostWithForwardedMessage(
                override val post: IStandalonePost,
                override val answers: List<IAnswerPost>,
                override val index: Long = -1L,
                override val forwardedPosts: List<IBasePost>
            ) : IndexedItem(), ForwardedMessage
        }

        sealed class ThreadItem : PostItem() {
            sealed class Answer : ThreadItem() {
                data class AnswerPost(override val post: IAnswerPost, override val index: Long = -1L) :
                    Answer()

                data class AnswerPostWithForwardedMessage(
                    override val post: IAnswerPost,
                    override val index: Long = -1L,
                    override val forwardedPosts: List<IBasePost>
                ) : Answer(), ForwardedMessage
            }

            sealed class ContextItem : ThreadItem() {
                data class ContextPost(override val post: IStandalonePost, override val index: Long = -1L) :
                    ContextItem()

                data class ContextPostWithForwardedMessage(
                    override val post: IStandalonePost,
                    override val index: Long = -1L,
                    override val forwardedPosts: List<IBasePost>
                ) : ContextItem(), ForwardedMessage
            }
        }

        fun isThreadContextItem(): Boolean = this is ThreadItem.ContextItem
    }

    data class DateDivider(val localDate: LocalDate) : ChatListItem()

    data object UnreadIndicator : ChatListItem()
}
