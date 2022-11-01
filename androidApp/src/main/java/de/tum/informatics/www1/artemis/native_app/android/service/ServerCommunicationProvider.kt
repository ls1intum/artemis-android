package de.tum.informatics.www1.artemis.native_app.android.service

import de.tum.informatics.www1.artemis.native_app.android.server_config.ProfileInfo
import de.tum.informatics.www1.artemis.native_app.android.util.DataState
import kotlinx.coroutines.flow.Flow

interface ServerCommunicationProvider {

    /**
     * Emits the currently selected server. Emits again, when the user changes their artemis instance in the settings.
     */
    val serverUrl: Flow<String>

    /**
     * Just returns the domain of the serverUrl.
     */
    val host: Flow<String>

    /**
     * The profile info associated with the server url.
     */
    val serverProfileInfo: Flow<DataState<ProfileInfo>>

    suspend fun updateServerUrl(serverUrl: String)

    suspend fun retryLoadServerProfileInfo()
}