package de.tum.informatics.www1.artemis.native_app.service.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Provides data about which instance of artemis is communicated with.
 */
class ServerCommunicationProvider {

    /**
     * Emits the currently selected server. Emits again, when the user changes their artemis instance in the settings.
     */
    val serverUrl: Flow<String> = flow { emit("https://artemis.ase.in.tum.de/") }
}