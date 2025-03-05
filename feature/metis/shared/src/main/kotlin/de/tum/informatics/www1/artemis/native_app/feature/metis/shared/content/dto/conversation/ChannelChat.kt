package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("channel")
data class ChannelChat(
    override val id: Long = 0,
    override val creationDate: Instant? = null,
    override val lastMessageDate: Instant? = null,
    override val creator: ConversationUser? = null,
    override val lastReadDate: Instant? = null,
    override val unreadMessagesCount: Long? = 0L,
    override val isFavorite: Boolean = false,
    override val isHidden: Boolean = false,
    override val isMuted: Boolean = false,
    override val isCreator: Boolean = false,
    override val isMember: Boolean = false,
    override val numberOfMembers: Int = 0,
    val subTypeReferenceId: Long? = null, // Id of the corresponding lecture, exercise, exam etc that is related to the channel.
    val name: String = "",
    val description: String? = null,
    val topic: String? = null,
    val isPublic: Boolean = true,
    val isAnnouncementChannel: Boolean = false,
    val isArchived: Boolean = false,
    val isCourseWide: Boolean = false,
    val hasChannelModerationRights: Boolean = false, // A course instructor has channel moderation rights but is not necessarily a moderator of the channel
    val isChannelModerator: Boolean = false,  // Member of the channel that is also a moderator of the channel
    val tutorialGroupId: Long? = null,
    val tutorialGroupTitle: String? = null
) : Conversation() {
    override val typeAsString: String = "channel"

    override fun withUnreadMessagesCount(unreadMessagesCount: Long): Conversation =
        copy(unreadMessagesCount = unreadMessagesCount)

    override fun filterPredicate(query: String): Boolean {
        return name.contains(query, ignoreCase = true)
    }
}
