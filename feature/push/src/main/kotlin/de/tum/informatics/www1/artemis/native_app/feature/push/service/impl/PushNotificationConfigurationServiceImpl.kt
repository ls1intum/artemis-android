package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
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

        // Enable or disable generating of token which are automatically uploaded to Google.
        // This is persisted among app restarts
        firebaseMessaging.isAutoInitEnabled = newIsEnabled

        val result: NetworkResponse<HttpStatusCode> = if (newIsEnabled) {
            val newAesKey = refreshAESKey()

            // First generate or get the current token.
            val fireBaseToken = try {
                firebaseMessaging.token.await()
            } catch (e: Exception) {
                Log.e(TAG, "Something went wrong while getting/generating firebase token", e)
                return false
            }

            // Set the token.
            storeFirebaseToken(fireBaseToken)

            uploadPushNotificationDeviceConfigurationsToServer(
                serverUrl = serverUrl,
                authToken = authToken,
                aesKey = newAesKey,
                firebaseToken = fireBaseToken
            )
        } else {
            try {
                firebaseMessaging.deleteToken().await()
                FirebaseInstallations.getInstance().delete().await()
            } catch (e: Exception) {
                Log.e(TAG, "Something went wrong while deleting token and installation", e)
                return false
            }

            unsubscribeFromNotifications(
                serverUrl = serverUrl,
                authToken = authToken
            )
        }

        return if (result is NetworkResponse.Response && result.data.isSuccess()) {
            context.pushNotificationsStore.edit { data ->
                data[getPushNotificationEnabledKeyForServer(serverUrl)] = newIsEnabled
            }
            true
        } else {
            false
        }
    }

    override suspend fun storeFirebaseToken(token: String) {
        context.pushNotificationsStore.edit { data ->
            data[FIREBASE_TOKEN_KEY] = token
        }
    }

    override suspend fun refreshAESKey(): SecretKey {
        val aesKey = withContext(Dispatchers.IO) {
            val generator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEY_STORE
            )
            generator.init(
                KeyGenParameterSpec
                    .Builder(
                        KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                    .setRandomizedEncryptionRequired(true)
                    .build()
            )
            generator.generateKey()
        }

        return aesKey
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

    override suspend fun getOrCreateCurrentAESKey(): SecretKey {
        return getCurrentAESKey() ?: refreshAESKey()
    }

    private fun getAndroidKeystore() = KeyStore.getInstance(ANDROID_KEY_STORE).apply {
        load(null)
    }

    override suspend fun uploadPushNotificationDeviceConfigurationsToServer(
        serverUrl: String,
        authToken: String,
        aesKey: SecretKey,
        firebaseToken: String
    ): NetworkResponse<HttpStatusCode> {
        // TODO("Not yet implemented")
        return NetworkResponse.Response(HttpStatusCode.OK)
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