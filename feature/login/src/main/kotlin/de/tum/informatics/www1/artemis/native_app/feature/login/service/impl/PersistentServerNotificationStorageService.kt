package de.tum.informatics.www1.artemis.native_app.feature.login.service.impl

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import de.tum.informatics.www1.artemis.native_app.feature.login.service.ServerNotificationStorageService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PersistentServerNotificationStorageService(private val context: Context) :
    ServerNotificationStorageService {

    private val Context.store by preferencesDataStore("server_notification_storage")

    override suspend fun hasDisplayedForServer(serverUrl: String): Boolean {
        return context.store.data.map { it[getKey(serverUrl)] ?: false }.first()
    }

    override suspend fun setHasDisplayed(serverUrl: String) {
        context.store.edit {
            it[getKey(serverUrl)] = true
        }
    }

    private fun getKey(serverUrl: String): Preferences.Key<Boolean> =
        booleanPreferencesKey(serverUrl)
}