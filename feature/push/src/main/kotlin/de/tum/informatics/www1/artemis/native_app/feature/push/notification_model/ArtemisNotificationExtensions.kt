package de.tum.informatics.www1.artemis.native_app.feature.push.notification_model

/**
 * Extension functions to work with the courseNotificationDTO structure
 * for notification handling logic.
 */

/**
 * Gets the notification placeholders extracted from courseNotificationDTO data.
 */
fun ArtemisNotification<*>.getNotificationPlaceholders(): List<String> {
    return CourseNotificationDTOExtractor.extractNotificationPlaceholders(courseNotificationDTO)
}

/**
 * Gets the notification type string from courseNotificationDTO.
 */
fun ArtemisNotification<*>.getNotificationType(): NotificationType {
    return CourseNotificationDTOExtractor.getNotificationType(courseNotificationDTO)
}

/**
 * Gets the notification category from courseNotificationDTO.
 */
fun ArtemisNotification<*>.getNotificationCategory(): NotificationCategory {
    return courseNotificationDTO.category
}

