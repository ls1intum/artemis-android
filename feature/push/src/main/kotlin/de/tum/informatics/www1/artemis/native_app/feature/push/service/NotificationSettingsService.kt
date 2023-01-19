package de.tum.informatics.www1.artemis.native_app.feature.push.service

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.feature.push.ui.model.PushNotificationSetting

/**
 * Service that handles changing which notifications the user wants to receive.
 */
internal interface NotificationSettingsService {

    suspend fun getNotificationSettings(serverUrl: String, authToken: String): NetworkResponse<List<PushNotificationSetting>>

    suspend fun updateNotificationSettings(newSettings: List<PushNotificationSetting>, serverUrl: String, authToken: String): NetworkResponse<List<PushNotificationSetting>>
}