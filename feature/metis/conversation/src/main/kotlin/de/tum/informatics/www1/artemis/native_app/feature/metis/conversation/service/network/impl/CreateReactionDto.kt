package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.impl

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The body of "courses/{courseId}/postings/reactions".
 *
 * Its own type rather than the reaction model: the server takes the posting being reacted to as a
 * flat id, while the model carries the posting itself because that is how a reaction is read back.
 *
 * The id alone does not identify a posting. Messages and answer messages are numbered independently
 * on the server, so the same value regularly denotes one of each, and [postingType] says which one
 * is meant. Without it the server resolves the id within the course and rejects the request when
 * both exist there.
 */
@Serializable
internal data class CreateReactionDto(
    val emojiId: String,
    val relatedPostId: Long,
    val postingType: PostingType
)

@Serializable
internal enum class PostingType {
    @SerialName("POST")
    POST,

    @SerialName("ANSWER")
    ANSWER
}
