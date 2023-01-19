package de.tum.informatics.www1.artemis.native_app.feature.push

import com.google.firebase.messaging.FirebaseMessagingService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationConfigurationService
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationJobService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.get

class ArtemisFirebaseMessagingService : FirebaseMessagingService() {

    private val pushNotificationJobService: PushNotificationJobService = get()
    private val pushNotificationConfigurationService: PushNotificationConfigurationService = get()
    private val serverConfigurationService: ServerConfigurationService = get()

    /**
     * Whenever this functions is called we need to synchronize the new token with the server.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // Sent this new token to the server if push is enabled
        runBlocking {
            val currentToken = pushNotificationConfigurationService.firebaseToken.first()
            val serverUrl = serverConfigurationService.serverUrl.first()

            val arePushNotificationsEnabled = pushNotificationConfigurationService
                .getArePushNotificationsEnabledFlow(serverUrl)
                .first()

            // Only upload when push notifications are already enabled
            if (arePushNotificationsEnabled && currentToken != token) {
                pushNotificationConfigurationService.storeFirebaseToken(token)

                // Schedule a task
                pushNotificationJobService.scheduleUploadPushNotificationDeviceConfigurationToServer()
            }
        }
    }
}