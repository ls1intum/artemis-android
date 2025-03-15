package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto

import kotlinx.serialization.Serializable

@Serializable
data class ForwardedMessage(
    override val sourceId: Long?,
    override val sourceType: PostingType?,
    override val destinationPostId: Long? = null,
    override val destinationAnswerPostId: Long? = null,
    val id: Long? = null,
    val content: String? = null
) : IForwardedMessage {

    init {
        if (!validateDestinations()) {
            throw IllegalArgumentException("A forwarded message must have exactly one destination")
        }
    }

    private fun validateDestinations(): Boolean {
        val isDestinationPostValid = this.destinationPostId != null
        val isDestinationAnswerPostValid = this.destinationAnswerPostId != null
        return (isDestinationPostValid && !isDestinationAnswerPostValid) || (!isDestinationPostValid && isDestinationAnswerPostValid)
    }
}