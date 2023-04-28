package de.tum.informatics.www1.artemis.native_app.feature.metis.content

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
    override val unreadMessagesCount: Long = 0L,
    override val isFavorite: Boolean = false,
    override val isHidden: Boolean = false,
    override val isCreator: Boolean = false,
    override val isMember: Boolean = false,
    override val numberOfMembers: Int = 0,
    val name: String = "",
    val description: String? = null,
    val topic: String? = null,
    val isPublic: Boolean = true,
    val isAnnouncementChannel: Boolean = false,
    val isArchived: Boolean = false,
    val hasChannelModerationRights: Boolean = false,
    val isChannelModerator: Boolean = false,
    val tutorialGroupId: Long? = null,
    val tutorialGroupTitle: String? = null
) : Conversation()
