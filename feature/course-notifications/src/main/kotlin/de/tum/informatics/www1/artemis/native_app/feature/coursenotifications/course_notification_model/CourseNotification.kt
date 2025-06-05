package de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.course_notification_model

import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.ArtemisNotification
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.NotificationType
import java.util.Date


data class CourseNotification(
    val notificationType: NotificationType,
    val notificationId: Int,
    val courseId: Int,
    val creationDate: Date,
    val category: NotificationCategory,
    val status: NotificationStatus,
    val notification: ArtemisNotification<NotificationType>
) {
    val id: Int get() = notificationId
}


enum class NotificationCategory {
    COMMUNICATION,
    GENERAL,
    UNKNOWN
}

enum class NotificationStatus {
    UNSEEN,
    SEEN,
    UNKNOWN
}
