package de.tum.informatics.www1.artemis.native_app.core.model.metis

import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Reaction(
    val id: Int? = null,
    val user: User? = null,
    val creationDate: Instant? = null,
    val emojiId: String? = null,
    @SerialName("post")
    val standalonePost: StandalonePost? = null,
    val answerPost: AnswerPost? = null,
)