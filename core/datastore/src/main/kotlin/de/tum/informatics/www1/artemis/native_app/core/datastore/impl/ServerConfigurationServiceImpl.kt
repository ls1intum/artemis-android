package de.tum.informatics.www1.artemis.native_app.core.datastore.impl

import android.content.Context
import android.provider.Settings.Global
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.defaults.ArtemisInstances
import io.ktor.http.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

/**
 * Provides data about which instance of artemis is communicated with.
 */
internal class ServerConfigurationServiceImpl(
    private val context: Context
) :
    ServerConfigurationService {

    private companion object {
        private val Context.serverCommunicationPreferences by preferencesDataStore("server_communication")

        private val SERVER_URL_KEY = stringPreferencesKey("server_url")
        private val HAS_SELECTED_INSTANCE_KEY = booleanPreferencesKey("has_selected_instance")
    }

    @OptIn(DelicateCoroutinesApi::class)
    override val serverUrl: Flow<String> =
        context
            .serverCommunicationPreferences
            .data
            .map { data -> data[SERVER_URL_KEY] ?: ArtemisInstances.TUM_ARTEMIS.serverUrl }
            .distinctUntilChanged()
            .shareIn(GlobalScope, SharingStarted.Eagerly, replay = 1)

    override val host: Flow<String> =
        serverUrl
            .map { Url(it).host }

    override val hasUserSelectedInstance: Flow<Boolean> =
        context
            .serverCommunicationPreferences
            .data
            .map { it[HAS_SELECTED_INSTANCE_KEY] ?: false }

    override suspend fun updateServerUrl(serverUrl: String) {
        context.serverCommunicationPreferences.edit { data ->
            data[SERVER_URL_KEY] = serverUrl
            data[HAS_SELECTED_INSTANCE_KEY] = true
        }
    }
}