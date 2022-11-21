package de.tum.informatics.www1.artemis.native_app.core.model.exercise.feedback

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
class FeedbackConflict(
    val id: Int? = null,
    val conflict: Boolean? = null,
    val conflictingFeedbackId: Int? = null,
    val createdAt: Instant? = null,
    val solvedAt: Instant? = null,
    val type: FeedbackConflictType? = null,
    val discard: Boolean? = null,
) {
    @Serializable
    enum class FeedbackConflictType {
        INCONSISTENT_COMMENT,
        INCONSISTENT_SCORE,
        INCONSISTENT_FEEDBACK
    }
}