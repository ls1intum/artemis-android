package de.tum.informatics.www1.artemis.native_app.core.datastore.impl

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import de.tum.informatics.www1.artemis.native_app.core.datastore.PushNotificationConfigurationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class PushNotificationConfigurationServiceImpl(
    private val context: Context
) : PushNotificationConfigurationService {

    companion object {
        private val ARE_PUSH_NOTIFICATIONS_ENABLED_KEY =
            booleanPreferencesKey("push_notifications_enabled")

        private const val KEY_ALIAS = "pushNotificationKey"

        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
    }

    private val Context.pushNotificationsStore by preferencesDataStore("push_notifications_store")

    override val arePushNotificationEnabled: Flow<Boolean> =
        context.pushNotificationsStore.data.map { data ->
            data[ARE_PUSH_NOTIFICATIONS_ENABLED_KEY] ?: false
        }

    override suspend fun updateArePushNotificationEnabled(newIsEnabled: Boolean) {
        context.pushNotificationsStore.edit { data ->
            data[ARE_PUSH_NOTIFICATIONS_ENABLED_KEY] = newIsEnabled
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
}