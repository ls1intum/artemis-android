package de.tum.informatics.www1.artemis.native_app.feature.push

import android.util.Base64
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.ArtemisNotification
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.CommunicationNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.MiscNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationConfigurationService
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationJobService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.get
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

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

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(TAG, "New push notification received")

        val payloadCiphertext = message.data["payload"] ?: return
        val iv = message.data["iv"] ?: return

        val payload: String = runBlocking {
            val key =
                pushNotificationConfigurationService.getCurrentAESKey() ?: return@runBlocking null
            val cipher = cipher ?: return@runBlocking null

            val ivAsBytes = Base64.decode(iv.toByteArray(Charsets.ISO_8859_1), Base64.DEFAULT)

            cipher.decrypt(payloadCiphertext, key, ivAsBytes) ?: return@runBlocking null
        } ?: return

        val notification: ArtemisNotification = Json.decodeFromString(payload)


    }

    private fun Cipher.decrypt(ciphertext: String, key: SecretKey, iv: ByteArray): String? {
        return try {
            init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))

            val cipherTextBytes = ciphertext.toByteArray(Charsets.ISO_8859_1)
            val textBytes = doFinal(Base64.decode(cipherTextBytes, Base64.DEFAULT))

            String(textBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }
}
