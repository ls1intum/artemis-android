package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto

import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.AnswerPostPojo
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class AnswerPost(
    override val id: Long? = null,
    override val author: User? = null,
    override val authorRole: UserRole? = null,
    override val creationDate: Instant? = null,
    override val updatedDate: Instant? = null,
    override val content: String? = null,
    override val reactions: List<Reaction>? = null,
    @SerialName("resolvesPost")
    override val resolvesPost: Boolean = false,
    val post: StandalonePost? = null
) : BasePost(), IAnswerPost {

    @Transient
    override val authorId: Long? = author?.id

    @Transient
    override val serverPostId: Long? = id

    @Transient
    override val clientPostId: String? = null

    constructor(answerPostDb: AnswerPostPojo, post: StandalonePost) : this(
        id = answerPostDb.serverPostId,
        author = User(id = answerPostDb.authorId),
        content = answerPostDb.content,
        creationDate = answerPostDb.creationDate,
        post = post
    )
}