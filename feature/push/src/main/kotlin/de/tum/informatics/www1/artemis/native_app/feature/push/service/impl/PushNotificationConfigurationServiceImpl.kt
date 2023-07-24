package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl

import android.content.Context
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProtection
import android.util.Base64
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationConfigurationService
import de.tum.informatics.www1.artemis.native_app.feature.push.service.network.NotificationSettingsService
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.security.KeyStore
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@OptIn(DelicateCoroutinesApi::class)
class PushNotificationConfigurationServiceImpl internal constructor(
    private val context: Context,
    private val notificationSettingsService: NotificationSettingsService
) : PushNotificationConfigurationService {

    companion object {
        private val FIREBASE_TOKEN_KEY = stringPreferencesKey("firebase_token")

        private const val KEY_ALIAS = "pushNotificationKey"

        private const val ANDROID_KEY_STORE = "AndroidKeyStore"

        private const val TAG = "PushNotificationConfigurationServiceImpl"
    }

    private val Context.pushNotificationsStore by preferencesDataStore("push_notifications_store")

    override val firebaseToken: Flow<String?> = context
        .pushNotificationsStore
        .data
        .map { data ->
            data[FIREBASE_TOKEN_KEY]
        }
        .shareIn(GlobalScope, SharingStarted.Lazily, replay = 1)

    override fun getArePushNotificationsEnabledFlow(serverUrl: String): Flow<Boolean> {
        return context
            .pushNotificationsStore
            .data
            .map { data ->
                data[getPushNotificationEnabledKeyForServer(serverUrl)] ?: false
            }
    }

    override suspend fun updateArePushNotificationEnabled(
        newIsEnabled: Boolean,
        serverUrl: String,
        authToken: String
    ): Boolean {
        val firebaseMessaging = FirebaseMessaging.getInstance()

        val prevIsAutoInitEnabled = firebaseMessaging.isAutoInitEnabled

        // Enable or disable generating of token which are automatically uploaded to Google.
        // This is persisted among app restarts
        firebaseMessaging.isAutoInitEnabled = newIsEnabled

        val isSuccess = if (newIsEnabled) {
            // First generate or get the current token.
            val fireBaseToken = try {
                firebaseMessaging.token.await()
            } catch (e: Exception) {
                Log.e(TAG, "Something went wrong while getting/generating firebase token", e)
                firebaseMessaging.isAutoInitEnabled = prevIsAutoInitEnabled
                return false
            }

            // Set the token.
            storeFirebaseToken(fireBaseToken)

            val secretKeyResponse =
                notificationSettingsService.uploadPushNotificationDeviceConfigurationsToServer(
                    serverUrl = serverUrl,
                    authToken = authToken,
                    firebaseToken = fireBaseToken
                )

            when (secretKeyResponse) {
                is NetworkResponse.Failure -> false
                is NetworkResponse.Response -> {
                    // Store decryption key
                    storeAESKey(secretKeyResponse.data)
                    true
                }
            }
        } else {
            val currentToken = firebaseToken.first()
            if (currentToken != null) {
                try {
                    firebaseMessaging.deleteToken().await()
                    FirebaseInstallations.getInstance().delete().await()

                    storeFirebaseToken(null)

                    // The result does not matter. This device does no longer receive notifications
                    notificationSettingsService.unsubscribeFromNotifications(
                        serverUrl = serverUrl,
                        authToken = authToken,
                        firebaseToken = currentToken
                    )

                    true
                } catch (e: Exception) {
                    Log.e(TAG, "Something went wrong while deleting token and installation", e)
                    firebaseMessaging.isAutoInitEnabled = prevIsAutoInitEnabled
                    false
                }
            } else true
        }

        return if (isSuccess) {
            context.pushNotificationsStore.edit { data ->
                data[getPushNotificationEnabledKeyForServer(serverUrl)] = newIsEnabled
            }
            true
        } else {
            firebaseMessaging.isAutoInitEnabled = prevIsAutoInitEnabled
            false
        }
    }

    override suspend fun storeFirebaseToken(token: String?) {
        context.pushNotificationsStore.edit { data ->
            if (token != null) {
                data[FIREBASE_TOKEN_KEY] = token
            } else {
                data.remove(FIREBASE_TOKEN_KEY)
            }
        }
    }

    override suspend fun getCurrentAESKey(): SecretKey? {
        return withContext(Dispatchers.IO) {
            val keyStore = getAndroidKeystore()
            when (val entry = keyStore.getEntry(KEY_ALIAS, null)) {
                is KeyStore.SecretKeyEntry -> entry.secretKey
                else -> null
            }
        }
    }

    override suspend fun storeAESKey(key: SecretKey) {
        withContext(Dispatchers.IO) {
            getAndroidKeystore()
                .setEntry(
                    KEY_ALIAS,
                    KeyStore.SecretKeyEntry(key),
                    KeyProtection.Builder(
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .setRandomizedEncryptionRequired(true)
                        .build()
                )
        }
    }

    private fun getAndroidKeystore() = KeyStore.getInstance(ANDROID_KEY_STORE).apply {
        load(null)
    }

    private fun getPushNotificationEnabledKeyForServer(serverUrl: String) =
        booleanPreferencesKey("push_enabled_$serverUrl")
}
