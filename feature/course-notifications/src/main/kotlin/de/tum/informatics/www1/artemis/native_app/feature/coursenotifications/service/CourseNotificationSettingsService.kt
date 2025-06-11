package de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.service

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.model.NotificationChannel
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.model.NotificationSettings
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.model.NotificationSettingsInfo

interface CourseNotificationSettingsService {
    /**
     * Get Notification Settings Info
     */
    suspend fun getNotificationSettingsInfo(
        serverUrl: String,
        authToken: String
    ): NetworkResponse<NotificationSettingsInfo>

    /**
     * Get Notification Settings for a specific course
     */
    suspend fun getNotificationSettings(
        courseId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<NotificationSettings>

    /**
     * Update Notification Settings for the given notification type
     */
    suspend fun updateSetting(
        courseId: Long,
        setting: NotificationSettings,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Unit>

    /**
     * Use a notification preset from the server
     */
    suspend fun selectPreset(
        courseId: Long,
        presetId: Int,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Unit>
} 