package de.tum.informatics.www1.artemis.native_app.android.service

import de.tum.informatics.www1.artemis.native_app.android.content.ProfileInfo
import de.tum.informatics.www1.artemis.native_app.android.util.DataState
import de.tum.informatics.www1.artemis.native_app.android.util.NetworkResponse
import kotlinx.coroutines.flow.Flow

interface ServerCommunicationProvider {

    /**
     * Emits the currently selected server. Emits again, when the user changes their artemis instance in the settings.
     */
    val serverUrl: Flow<String>

    /**
     * The profile info associated with the server url.
     */
    val serverProfileInfo: Flow<DataState<ProfileInfo>>

    suspend fun updateServerUrl(serverUrl: String)
}