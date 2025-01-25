package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
sealed class Conversation {
    abstract val id: Long
    abstract val creationDate: Instant?
    abstract val lastMessageDate: Instant?
    abstract val creator: ConversationUser?
    abstract val lastReadDate: Instant?
    abstract val unreadMessagesCount: Long?
    abstract val isFavorite: Boolean
    abstract val isHidden: Boolean
    abstract val isMuted: Boolean
    abstract val isCreator: Boolean
    abstract val isMember: Boolean
    abstract val numberOfMembers: Int

    abstract val typeAsString: String

    abstract fun withUnreadMessagesCount(unreadMessagesCount: Long): Conversation
    abstract fun filterPredicate(query: String): Boolean
}

val Conversation.hasModerationRights: Boolean
    get() = when(this) {
        is ChannelChat -> isChannelModerator
        is GroupChat -> isCreator
        is OneToOneChat -> false
    }