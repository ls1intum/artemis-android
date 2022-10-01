package de.tum.informatics.www1.artemis.native_app.android.service.impl

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.tum.informatics.www1.artemis.native_app.android.defaults.ArtemisInstances
import de.tum.informatics.www1.artemis.native_app.android.service.ServerCommunicationProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Provides data about which instance of artemis is communicated with.
 */
class ServerCommunicationProviderImpl(context: Context) : ServerCommunicationProvider {

    private companion object {
        private val Context.serverCommunicationPreferences by preferencesDataStore("server_communication")

        private val SERVER_URL_KEY = stringPreferencesKey("server_url")
    }

    override val serverUrl: Flow<String> =
        context
            .serverCommunicationPreferences
            .data
            .map { data -> data[SERVER_URL_KEY] ?: ArtemisInstances.TUM_ARTEMIS.serverUrl }
}