package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The context in which metis is used, e.g. in a course, an exercise or personal communication.
 */
@Serializable
sealed class MetisContext{
    abstract val courseId: Long

    @Serializable
    @SerialName("course")
    data class Course(override val courseId: Long) : MetisContext()

    @Serializable
    @SerialName("conversation")
    data class Conversation(override val courseId: Long, val conversationId: Long) : MetisContext()
}
