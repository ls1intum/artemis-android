package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto

import de.tum.informatics.www1.artemis.native_app.core.model.account.User
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
    override val conversation: ISavedPost.SimpleConversationInfo,
    override val postingType: SavedPostPostingType,
    override val referencePostId: Long,
    override val authorRole: UserRole? = null,
    override val reactions: List<Reaction>? = null,
    override val hasForwardedMessages: Boolean? = null,
    override val forwardedPosts: List<IStandalonePost>? = null,
    override val forwardedAnswerPosts: List<IAnswerPost>? = null
) : BasePost(), ISavedPost {


}