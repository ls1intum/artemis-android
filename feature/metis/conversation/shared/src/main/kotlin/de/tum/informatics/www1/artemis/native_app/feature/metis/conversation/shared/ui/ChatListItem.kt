package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.ui

import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IAnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.ISavedPost
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
        val key: Any
            get() = post.key

        abstract fun copy(post: IBasePost = this.post): PostItem

        interface ForwardedMessage {
            val forwardedPosts: List<IBasePost?>
            val courseId: Long
        }

        sealed class IndexedItem : PostItem() {
            abstract val answers: List<IAnswerPost>
            abstract val index: Long

            data class Post(
                override val post: IStandalonePost,
                override val answers: List<IAnswerPost>,
                override val index: Long = -1L
            ) : IndexedItem() {
                override fun copy(post: IBasePost) = copy(post = post as IStandalonePost, index = index, answers = answers)
            }

            data class PostWithForwardedMessage(
                override val post: IStandalonePost,
                override val answers: List<IAnswerPost>,
                override val index: Long = -1L,
                override val forwardedPosts: List<IBasePost?>,
                override val courseId: Long
            ) : IndexedItem(), ForwardedMessage {
                override fun copy(post: IBasePost) = copy(post = post as IStandalonePost, index = index, answers = answers, forwardedPosts = forwardedPosts, courseId = courseId)
            }
        }

        sealed class ThreadItem : PostItem() {
            sealed class Answer : ThreadItem() {
                data class AnswerPost(override val post: IAnswerPost) :
                    Answer() {
                    override fun copy(post: IBasePost) = copy(post = post as IAnswerPost)
                    }

                data class AnswerPostWithForwardedMessage(
                    override val post: IAnswerPost,
                    override val forwardedPosts: List<IBasePost?>,
                    override val courseId: Long
                ) : Answer(), ForwardedMessage {
                    override fun copy(post: IBasePost) = copy(post = post as IAnswerPost, forwardedPosts = forwardedPosts, courseId = courseId)
                }
            }

            sealed class ContextItem : ThreadItem() {
                data class ContextPost(override val post: IStandalonePost) :
                    ContextItem() {
                    override fun copy(post: IBasePost) = copy(post = post as IStandalonePost)
                    }

                data class ContextPostWithForwardedMessage(
                    override val post: IStandalonePost,
                    override val forwardedPosts: List<IBasePost?>,
                    override val courseId: Long
                ) : ContextItem(), ForwardedMessage {
                    override fun copy(post: IBasePost) = copy(post = post as IStandalonePost, forwardedPosts = forwardedPosts, courseId = courseId)
                }
            }
        }

        sealed class SavedItem: PostItem() {
            data class SavedPost(
                override val post: ISavedPost
            ) : SavedItem() {
                override fun copy(post: IBasePost): PostItem = copy(post = post as ISavedPost)
            }

            data class SavedPostWithForwardedMessage(
                override val post: ISavedPost,
                override val forwardedPosts: List<IBasePost?>,
                override val courseId: Long
            ) : SavedItem(), ForwardedMessage {
                override fun copy(post: IBasePost) = copy(post = post as ISavedPost, forwardedPosts = forwardedPosts, courseId = courseId)
            }
        }

        fun isThreadContextItem(): Boolean = this is ThreadItem.ContextItem
    }

    data class DateDivider(val localDate: LocalDate) : ChatListItem()

    data object UnreadIndicator : ChatListItem()
}
