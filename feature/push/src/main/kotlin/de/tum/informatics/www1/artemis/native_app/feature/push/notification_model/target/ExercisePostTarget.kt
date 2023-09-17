package de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.target

import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.content.MetisContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExercisePostTarget(
    @SerialName("id")
    override val postId: Long,
    @SerialName("course")
    val courseId: Long,
    @SerialName("exercise")
    val exerciseId: Long
) : MetisTarget {
    override val metisContext: de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.content.MetisContext = de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.content.MetisContext.Exercise(courseId, exerciseId)
}