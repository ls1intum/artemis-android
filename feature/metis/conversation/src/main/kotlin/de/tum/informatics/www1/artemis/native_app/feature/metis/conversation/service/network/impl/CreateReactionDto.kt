package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.impl

import kotlinx.serialization.Serializable

/**
 * The body of "courses/{courseId}/postings/reactions".
 *
 * Its own type rather than the reaction model: the server takes the id of the posting being reacted
 * to as a flat, required field, while the model carries the posting itself because that is how a
 * reaction is read back.
 */
@Serializable
internal data class CreateReactionDto(
    val emojiId: String,
    val relatedPostId: Long
)
