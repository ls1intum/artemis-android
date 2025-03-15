package de.tum.informatics.www1.artemis.native_app.feature.push.service.network

import de.tum.informatics.www1.artemis.native_app.core.common.app_version.NormalizedAppVersion
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.feature.push.ui.model.PushNotificationSetting
import io.ktor.http.HttpStatusCode
import javax.crypto.SecretKey

/**
 * Service that handles changing which notifications the user wants to receive.
 */
internal interface NotificationSettingsService {

    suspend fun getNotificationSettings(serverUrl: String, authToken: String): NetworkResponse<List<PushNotificationSetting>>

    suspend fun updateNotificationSettings(newSettings: List<PushNotificationSetting>, serverUrl: String, authToken: String): NetworkResponse<List<PushNotificationSetting>>

    /**
     * Registers this device to the server. If successful, a SecretKey (AES256) is returned
     */
    suspend fun uploadPushNotificationDeviceConfigurationsToServer(
        serverUrl: String,
        authToken: String,
        firebaseToken: String,
        appVersion: NormalizedAppVersion
    ): NetworkResponse<SecretKey>

    /**
     * The auth token matters because push notifications are bound to the jwt
     */
    suspend fun unsubscribeFromNotifications(
        serverUrl: String,
        authToken: String,
        firebaseToken: String
    ): NetworkResponse<HttpStatusCode>
}