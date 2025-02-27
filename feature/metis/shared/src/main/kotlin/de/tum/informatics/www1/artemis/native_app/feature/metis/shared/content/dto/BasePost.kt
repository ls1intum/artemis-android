package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto

import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
sealed class BasePost : IBasePost {
    abstract val id: Long?
    abstract val author: User?
    abstract override val authorRole: UserRole?
    abstract override val creationDate: Instant?
    abstract override val updatedDate: Instant?
    abstract override val content: String?
    abstract override val reactions: List<Reaction>?
    abstract override val isSaved: Boolean?
    abstract override val hasForwardedMessages: Boolean?

    override val serverPostId: Long?
        get() = id

    override val clientPostId: String? = null

    override val authorId: Long?
        get() = author?.id

    override val authorName: String?
        get() = author?.name

    override val authorImageUrl: String?
        get() = author?.imageUrl
}