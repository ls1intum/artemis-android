package de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.service

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.LoggedInBasedService
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.course_notification_model.NotificationSettings
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.course_notification_model.NotificationSettingsInfo

interface CourseNotificationSettingsService : LoggedInBasedService {
    /**
     * Get Notification Settings Info
     */
    suspend fun getNotificationSettingsInfo(): NetworkResponse<NotificationSettingsInfo>

    /**
     * Get Notification Settings for a specific course
     */
    suspend fun getNotificationSettings(courseId: Long): NetworkResponse<NotificationSettings>

    /**
     * Update Notification Settings for the given notification type
     */
    suspend fun updateSetting(courseId: Long, setting: NotificationSettings): NetworkResponse<Unit>

    /**
     * Use a notification preset from the server
     */
    suspend fun selectPreset( courseId: Long, presetId: Int): NetworkResponse<Unit>
} 