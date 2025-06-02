package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Outdated: The context in which metis (communication) is used, e.g. in a course, an exercise or personal communication.
 * Since quite some time now, metis can only be used in the communication section, and not in other contexts anymore.
 *
 * This class should eventually be merged with [ArtemisContext] for an up-to-date context class.
 * Because this class is deeply used in the codebase, we did not remove it yet.
 */
@Serializable
sealed class MetisContext {
    abstract val courseId: Long

    @Serializable
    @SerialName("course")
    data class Course(override val courseId: Long) : MetisContext()

    @Serializable
    @SerialName("conversation")
    data class Conversation(override val courseId: Long, val conversationId: Long) : MetisContext()
}
