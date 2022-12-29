package de.tum.informatics.www1.artemis.native_app.core.datastore.impl

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import de.tum.informatics.www1.artemis.native_app.core.datastore.PushNotificationConfigurationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PushNotificationConfigurationServiceImpl(
    private val context: Context
) : PushNotificationConfigurationService {

    companion object {
        private val ARE_PUSH_NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("push_notifications_enabled")
    }

    private val Context.pushNotificationsStore by preferencesDataStore("push_notifications_store")

    override val arePushNotificationEnabled: Flow<Boolean> = context.pushNotificationsStore.data.map { data ->
        data[ARE_PUSH_NOTIFICATIONS_ENABLED_KEY] ?: false
    }

    override suspend fun updateArePushNotificationEnabled(newIsEnabled: Boolean) {
        context.pushNotificationsStore.edit { data ->
            data[ARE_PUSH_NOTIFICATIONS_ENABLED_KEY] = newIsEnabled
        }
    }
}