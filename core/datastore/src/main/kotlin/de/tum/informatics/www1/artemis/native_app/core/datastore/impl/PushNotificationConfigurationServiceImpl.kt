package de.tum.informatics.www1.artemis.native_app.core.datastore.impl

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.MasterKey
import androidx.security.crypto.MasterKeys
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

        private val aesKeyGenerator: KeyGenerator by lazy {
            KeyGenerator.getInstance("AES").apply {
                init(256)
            }
        }
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

    override suspend fun refreshAESKey(username: String, serverId: String): SecretKey {
        val aesKey = withContext(Dispatchers.Default) {
            aesKeyGenerator.generateKey()
        }

        val entry = KeyStore.SecretKeyEntry(aesKey)
        withContext(Dispatchers.IO) {
            val keyStore = getAndroidKeystore()
            keyStore.setEntry(KEY_ALIAS, entry, null)
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

    private fun getAndroidKeystore() = KeyStore.getInstance("AndroidKeyStore")
}