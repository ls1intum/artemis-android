package de.tum.informatics.www1.artemis.native_app.core.model.metis

import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
sealed class BasePost : IBasePost {
    abstract val id: Long?
    abstract val author: User?
    abstract override val authorRole: UserRole?
    abstract override val creationDate: Instant?
    abstract override val content: String?
    abstract override val reactions: List<Reaction>?

    override val authorName: String?
        get() = author?.name
}