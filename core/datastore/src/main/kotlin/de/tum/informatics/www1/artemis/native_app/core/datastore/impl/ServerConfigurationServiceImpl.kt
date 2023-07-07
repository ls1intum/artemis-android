package de.tum.informatics.www1.artemis.native_app.core.datastore.impl

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.tum.informatics.www1.artemis.native_app.core.datastore.BuildConfig
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.defaults.ArtemisInstances
import io.ktor.http.Url
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
        private val HAS_SELECTED_INSTANCE_KEY = booleanPreferencesKey("has_selected_instance")
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

    override val host: Flow<String> =
        serverUrl
            .map { Url(it).host }

    /**
     * Use to decide if we want to show an instance selection UI to the user.
     * If [BuildConfig.hasInstanceRestriction] is set to true, we never want to show such a UI.
     */
    override val hasUserSelectedInstance: Flow<Boolean> =
        if (BuildConfig.hasInstanceRestriction) flowOf(true)
        else {
            context
                .serverCommunicationPreferences
                .data
                .map { it[HAS_SELECTED_INSTANCE_KEY] ?: false }
        }


    override suspend fun updateServerUrl(serverUrl: String) {
        context.serverCommunicationPreferences.edit { data ->
            data[SERVER_URL_KEY] = serverUrl
            data[HAS_SELECTED_INSTANCE_KEY] = true
        }
    }
}