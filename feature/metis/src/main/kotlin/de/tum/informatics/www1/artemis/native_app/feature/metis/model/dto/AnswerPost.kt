package de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto

import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnswerPost(
    override val id: Long = 0L,
    override val author: User? = null,
    override val authorRole: UserRole? = null,
    override val creationDate: Instant? = null,
    override val content: String? = null,
    override val reactions: List<Reaction>? = null,
    @SerialName("resolvedPost")
    override val resolvesPost: Boolean = false,
    val post: StandalonePost? = null
) : BasePost(), IAnswerPost {
    override val authorId: Long? = author?.id

    override val serverPostId: Long = id
}