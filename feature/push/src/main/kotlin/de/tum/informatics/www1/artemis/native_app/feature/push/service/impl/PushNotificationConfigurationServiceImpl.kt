package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl

import android.content.Context
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProtection
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationConfigurationService
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.security.KeyStore
import java.security.KeyStore.PasswordProtection
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

@OptIn(DelicateCoroutinesApi::class)
class PushNotificationConfigurationServiceImpl(
    private val context: Context
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
                uploadPushNotificationDeviceConfigurationsToServer(
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
            try {
                firebaseMessaging.deleteToken().await()
                FirebaseInstallations.getInstance().delete().await()
            } catch (e: Exception) {
                Log.e(TAG, "Something went wrong while deleting token and installation", e)
                firebaseMessaging.isAutoInitEnabled = prevIsAutoInitEnabled
                return false
            }

            val unsubscribeResult = unsubscribeFromNotifications(
                serverUrl = serverUrl,
                authToken = authToken
            )

            unsubscribeResult is NetworkResponse.Response && unsubscribeResult.data.isSuccess()
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

    override suspend fun storeFirebaseToken(token: String) {
        context.pushNotificationsStore.edit { data ->
            data[FIREBASE_TOKEN_KEY] = token
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
                        .setRandomizedEncryptionRequired(true)
                        .build()
                )
        }
    }

    private fun getAndroidKeystore() = KeyStore.getInstance(ANDROID_KEY_STORE).apply {
        load(null)
    }

    override suspend fun uploadPushNotificationDeviceConfigurationsToServer(
        serverUrl: String,
        authToken: String,
        firebaseToken: String
    ): NetworkResponse<SecretKey> {
        // TODO("Not yet implemented")

        // Mock implementation
        val secretKey: SecretKey =
            withContext(Dispatchers.Main) {
                KeyGenerator.getInstance("AES").apply {
                    init(256)
                }.generateKey()
            }

        return NetworkResponse.Response(secretKey)
    }

    override suspend fun unsubscribeFromNotifications(
        serverUrl: String,
        authToken: String
    ): NetworkResponse<HttpStatusCode> {
        // TODO("Not yet implemented")
        return NetworkResponse.Response(HttpStatusCode.OK)
    }

    private fun getPushNotificationEnabledKeyForServer(serverUrl: String) =
        booleanPreferencesKey("push_enabled_$serverUrl")
}