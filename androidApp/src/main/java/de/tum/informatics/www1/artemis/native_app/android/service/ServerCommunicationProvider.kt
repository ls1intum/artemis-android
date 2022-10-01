package de.tum.informatics.www1.artemis.native_app.android.service

import kotlinx.coroutines.flow.Flow

interface ServerCommunicationProvider {

    /**
     * Emits the currently selected server. Emits again, when the user changes their artemis instance in the settings.
     */
    val serverUrl: Flow<String>
}