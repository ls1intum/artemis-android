package de.tum.informatics.www1.artemis.native_app.feature.push

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationCipher
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationConfigurationService
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationHandler
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationJobService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.get
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.NoSuchPaddingException

class ArtemisFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "ArtemisFirebaseMessagingService"

        private const val Algorithm = "AES/CBC/PKCS7Padding"

        private val cipher: Cipher? = try {
            Cipher.getInstance(Algorithm)
        } catch (e: NoSuchAlgorithmException) {
            null
        } catch (e: NoSuchPaddingException) {
            null
        }
    }

    private val pushNotificationCipher: PushNotificationCipher = get()
    private val pushNotificationHandler: PushNotificationHandler = get()
    private val pushNotificationJobService: PushNotificationJobService = get()
    private val pushNotificationConfigurationService: PushNotificationConfigurationService = get()
    private val serverConfigurationService: ServerConfigurationService = get()

    /**
     * Whenever this functions is called we need to synchronize the new token with the server.
     */
    override fun onNewToken(token: String) {
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

    // In this method we only have 10 seconds to handle the notification
    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(TAG, "New push notification received")

        val payloadCiphertext = message.data["payload"] ?: return
        val iv = message.data["iv"] ?: return

        val payload: String? =
            pushNotificationCipher.decipherPushNotification(payloadCiphertext, iv)
        if (payload == null) {
            Log.d(TAG, "Could not decipher push notification")
            return
        }

        pushNotificationHandler.handleServerPushNotification(payload)
    }
}
