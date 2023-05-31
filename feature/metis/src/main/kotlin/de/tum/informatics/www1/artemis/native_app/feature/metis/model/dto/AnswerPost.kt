package de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto

import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.AnswerPostDb
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnswerPost(
    override val id: Long? = null,
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

    override val serverPostId: Long = id ?: 0L

    constructor(answerPostDb: AnswerPostDb, post: StandalonePost) : this(
        id = answerPostDb.serverPostId,
        author = User(id = answerPostDb.authorId),
        content = answerPostDb.content,
        creationDate = answerPostDb.creationDate,
        post = post
    )
}