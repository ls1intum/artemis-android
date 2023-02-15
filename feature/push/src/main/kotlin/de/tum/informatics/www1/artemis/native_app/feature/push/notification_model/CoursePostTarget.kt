package de.tum.informatics.www1.artemis.native_app.feature.push.notification_model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CoursePostTarget(
    @SerialName("id")
    val postId: Long,
    @SerialName("course")
    val courseId: Long
)