package de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.target

import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LecturePostTarget(
    @SerialName("id")
    override val postId: Long,
    @SerialName("course")
    val courseId: Long,
    @SerialName("lectureId")
    val lectureId: Long
) : MetisTarget {
    override val metisContext: MetisContext = MetisContext.Lecture(courseId, lectureId)
}