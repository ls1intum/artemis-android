package de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.course_notification_model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class NotificationChannel {
    @SerialName("WEBAPP")
    WEB,

    @SerialName("PUSH")
    PUSH,

    @SerialName("EMAIL")
    EMAIL,

    @SerialName("unknown")
    UNKNOWN
}