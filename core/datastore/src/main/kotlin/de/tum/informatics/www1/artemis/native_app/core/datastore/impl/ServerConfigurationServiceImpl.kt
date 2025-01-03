package de.tum.informatics.www1.artemis.native_app.core.datastore.impl

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.tum.informatics.www1.artemis.native_app.core.datastore.BuildConfig
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

/**
 * Provides data about which instance of artemis is communicated with.
 */
internal class ServerConfigurationServiceImpl(
    private val context: Context
) : ServerConfigurationService {

    private companion object {
        private val Context.serverCommunicationPreferences by preferencesDataStore("server_communication")

        private val SERVER_URL_KEY = stringPreferencesKey("server_url")
    }

    /**
     * Always emits [BuildConfig.defaultServerUrl] if [BuildConfig.hasInstanceRestriction] is set to true.
     * Otherwise, initially emits [BuildConfig.defaultServerUrl] until the user has selected their own instance.
     */
    @OptIn(DelicateCoroutinesApi::class)
    override val serverUrl: Flow<String> = if (BuildConfig.hasInstanceRestriction) {
        flowOf(BuildConfig.defaultServerUrl)
    } else {
        context
            .serverCommunicationPreferences
            .data
            .map { data -> data[SERVER_URL_KEY] ?: BuildConfig.defaultServerUrl }
            .distinctUntilChanged()
            .shareIn(GlobalScope, SharingStarted.Eagerly, replay = 1)
    }

    override suspend fun updateServerUrl(serverUrl: String) {
        context.serverCommunicationPreferences.edit { data ->
            data[SERVER_URL_KEY] = serverUrl
        }
    }
}