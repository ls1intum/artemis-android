package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto

import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class SavedPost(
    override val id: Long,
    override val author: User,
    override val creationDate: Instant? = null,
    override val updatedDate: Instant? = null,
    override val content: String?,
    override val isSaved: Boolean? = true,
    override val savedPostStatus: SavedPostStatus,
    override val conversation: Conversation,
    override val savedPostPostingType: SavedPostPostingType,
    override val referencePostId: Long,
    override val authorRole: UserRole? = null,
    override val reactions: List<Reaction>? = null,
) : BasePost(), ISavedPost {


}