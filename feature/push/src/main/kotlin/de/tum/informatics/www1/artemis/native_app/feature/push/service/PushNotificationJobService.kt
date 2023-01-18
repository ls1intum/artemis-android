package de.tum.informatics.www1.artemis.native_app.feature.push.service

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.Flow
import javax.crypto.SecretKey

interface PushNotificationJobService {

    /**
     * May return null if no firebase token has been set yet.
     */
    val firebaseToken: Flow<String?>

    suspend fun storeFirebaseToken(token: String)

    /**
     * Schedule a job that will upload the firebase id token and the AES key to the server.
     */
    fun scheduleUploadPushNotificationDeviceConfigurationToServer()

    /**
     * Cancels the job that tries to upload the configuration to the server.
     */
    suspend fun cancelPendingUploadPushNotificationDeviceConfigurationToServer()

    /**
     * Schedule a task that will tell the server that the user with the specified auth token no longer wants to receive
     * push notifications.
     */
    fun scheduleUnsubscribeFromNotifications(serverUrl: String, authToken: String)

    suspend fun uploadPushNotificationDeviceConfigurationsToServer(
        serverUrl: String,
        authToken: String,
        aesKey: SecretKey,
        firebaseToken: String
    ): NetworkResponse<HttpStatusCode>

    /**
     * The auth token matters because push notifications are bound to the jwt
     */
    suspend fun unsubscribeFromNotifications(
        serverUrl: String,
        authToken: String
    ): NetworkResponse<HttpStatusCode>
}
