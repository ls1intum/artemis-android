package de.tum.informatics.www1.artemis.native_app.android.service

import kotlinx.coroutines.flow.Flow

/**
 * Service that provides the current connectivity status of this device.
 */
interface NetworkStatusProvider {

    /**
     * Emits every time when the connectivity of this device to the internet changes.
     */
    val currentNetworkStatus: Flow<NetworkStatus>

    sealed class NetworkStatus {
        object Internet : NetworkStatus()
        object Unavailable : NetworkStatus()
    }
}