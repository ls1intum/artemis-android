package de.tum.informatics.www1.artemis.native_app.core.push_notification_settings.service

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.push_notification_settings.model.PushNotificationSetting

internal interface SettingsService {

    suspend fun getNotificationSettings(serverUrl: String, authToken: String): NetworkResponse<List<PushNotificationSetting>>

    suspend fun updateNotificationSettings(newSettings: List<PushNotificationSetting>, serverUrl: String, authToken: String): NetworkResponse<List<PushNotificationSetting>>
}