package de.tum.informatics.www1.artemis.native_app.feature.push.service

/**
 * Service that can schedule jobs related to the push notification synchronization with the server.
 * These jobs need to be guaranteed to be completed because otherwise weird behaviour can occur (e.g. users receiving push notifications for accounts they have already logged out from.)
 */
interface PushNotificationJobService {

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
    fun scheduleUnsubscribeFromNotifications(
        serverUrl: String,
        authToken: String,
        firebaseToken: String
    )
}
