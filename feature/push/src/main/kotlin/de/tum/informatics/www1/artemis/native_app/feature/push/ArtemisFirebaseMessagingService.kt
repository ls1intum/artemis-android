package de.tum.informatics.www1.artemis.native_app.feature.push

import android.content.Context
import android.util.Base64
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.JsonProvider
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationConfigurationService
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationJobService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import org.koin.android.ext.android.get
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class ArtemisFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val Algorithm = "AES/CBC/PKCS7Padding"

        private val cipher: Cipher? = try {
            Cipher.getInstance(Algorithm)
        } catch (e: NoSuchAlgorithmException) {
            null
        } catch (e: NoSuchPaddingException) {
            null
        }

        private val LatestPushNotificationId = intPreferencesKey("latestPushNotificationId")
    }

    private val pushNotificationJobService: PushNotificationJobService = get()
    private val pushNotificationConfigurationService: PushNotificationConfigurationService = get()
    private val serverConfigurationService: ServerConfigurationService = get()
    private val jsonProvider: JsonProvider = get()

    private val Context.notificationDataStore by preferencesDataStore("push_notification_ids")

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
        val payloadCiphertext = message.data["payload"] ?: return
        val iv = message.data["iv"] ?: return

        val payload: MessagePayload = runBlocking {
            val key =
                pushNotificationConfigurationService.getCurrentAESKey() ?: return@runBlocking null
            val cipher = cipher ?: return@runBlocking null

            val ivAsBytes = Base64.decode(iv.toByteArray(Charsets.ISO_8859_1), Base64.DEFAULT)

            val payloadText =
                cipher.decrypt(payloadCiphertext, key, ivAsBytes) ?: return@runBlocking null
            jsonProvider.applicationJsonConfiguration.decodeFromString(payloadText)
        } ?: return

        val title = payload.title
        val body = payload.body

        val notification = NotificationCompat.Builder(this, ArtemisNotificationChannel.id)
            .apply {
                if (title != null) setContentTitle(title)
                if (body != null) setContentText(title)
                setSmallIcon(R.drawable.push_notification_icon)
            }
            .build()

        val notificationId = runBlocking {
            val id = notificationDataStore.data.map { it[LatestPushNotificationId] ?: 0 }.first() + 1

            notificationDataStore.edit { data ->
                data[LatestPushNotificationId] = id
            }

            id
        }

        NotificationManagerCompat
            .from(this)
            .notify(notificationId, notification)
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

    @Serializable
    private data class MessagePayload(
        val title: String? = null,
        val body: String? = null,
        val target: String? = null,
        val type: String? = null
    )
}
