package de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto

import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Reaction(
    override val id: Long? = null,
    val user: User? = null,
    override val creationDate: Instant? = null,
    override val emojiId: String = "",
    @SerialName("post")
    val standalonePost: StandalonePost? = null,
    val answerPost: AnswerPost? = null,
) : IReaction {
    override val creatorId: Long? = user?.id
}