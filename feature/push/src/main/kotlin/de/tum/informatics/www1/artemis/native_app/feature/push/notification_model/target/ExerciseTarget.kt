package de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.target

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExerciseTarget(
    @SerialName("id")
    val exerciseId: Long,
    @SerialName("course")
    val courseId: Long
) : NotificationTarget