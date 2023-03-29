package de.tum.informatics.www1.artemis.native_app.feature.push.notification_model

import de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisContext
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
    override val metisContext: MetisContext = MetisContext.Exercise(courseId, exerciseId)
}