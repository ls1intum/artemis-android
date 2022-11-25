package de.tum.informatics.www1.artemis.native_app.core.model.metis

import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
sealed class BasePost {
    abstract val id: Int?
    abstract val author: User?
    abstract val authorRole: UserRole?
    abstract val creationDate: Instant?
    abstract val content: String?
    abstract val reactions: List<Reaction>?
}