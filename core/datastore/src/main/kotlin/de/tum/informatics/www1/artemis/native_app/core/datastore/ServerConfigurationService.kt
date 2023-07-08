package de.tum.informatics.www1.artemis.native_app.core.datastore

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface ServerConfigurationService {

    /**
     * Emits the currently selected server. Emits again, when the user changes their artemis instance in the settings.
     */
    val serverUrl: Flow<String>

    /**
     * Just returns the domain of the serverUrl.
     */
    val host: Flow<String>
        get() = serverUrl.map { Url(it).host }

    /**
     * If [updateServerUrl] has ever been called.
     */
    val hasUserSelectedInstance: Flow<Boolean>

    suspend fun updateServerUrl(serverUrl: String)
}