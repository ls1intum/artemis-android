package de.tum.informatics.www1.artemis.native_app.core.datastore

import de.tum.informatics.www1.artemis.native_app.android.model.server_config.ProfileInfo
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import kotlinx.coroutines.flow.Flow

interface ServerConfigurationService {

    /**
     * Emits the currently selected server. Emits again, when the user changes their artemis instance in the settings.
     */
    val serverUrl: Flow<String>

    /**
     * Just returns the domain of the serverUrl.
     */
    val host: Flow<String>

    suspend fun updateServerUrl(serverUrl: String)

    suspend fun retryLoadServerProfileInfo()
}