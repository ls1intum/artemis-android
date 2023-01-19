package de.tum.informatics.www1.artemis.native_app.feature.push.service

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.Flow
import javax.crypto.SecretKey

/**
 * Service that handles the persistent, device-related configurations of push notifications which
 * need to be kept in sync between client and server.
 */
interface PushNotificationConfigurationService {

    /**
     * May return null if no firebase token has been set yet.
     */
    val firebaseToken: Flow<String?>

    fun getArePushNotificationsEnabledFlow(serverUrl: String): Flow<Boolean>

    /**
     * Updates if push notifications have been enabled successfully.
     * If they are enabled, generated the necessary firebase token and uploads the configuration to the server.
     * If disabled, deletes the firebase token and the firebase installation and uploads these changes to the server.
     * @return if the synchronization was successful.
     */
    suspend fun updateArePushNotificationEnabled(
        newIsEnabled: Boolean,
        serverUrl: String,
        authToken: String
    ): Boolean

    /**
     * @return the current aes key or null if no key has been set yet.
     */
    suspend fun getCurrentAESKey(): SecretKey?

    suspend fun storeAESKey(key: SecretKey)

    suspend fun storeFirebaseToken(token: String)

    /**
     * Registers this device to the server. If successful, a SecretKey (AES256) is returned
     */
    suspend fun uploadPushNotificationDeviceConfigurationsToServer(
        serverUrl: String,
        authToken: String,
        firebaseToken: String
    ): NetworkResponse<SecretKey>

    /**
     * The auth token matters because push notifications are bound to the jwt
     */
    suspend fun unsubscribeFromNotifications(
        serverUrl: String,
        authToken: String
    ): NetworkResponse<HttpStatusCode>
}
