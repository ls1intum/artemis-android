package de.tum.informatics.www1.artemis.native_app.feature.metis.content

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("groupChat")
class GroupChat(
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
    val members: List<ConversationUser> = emptyList()
) : Conversation()
