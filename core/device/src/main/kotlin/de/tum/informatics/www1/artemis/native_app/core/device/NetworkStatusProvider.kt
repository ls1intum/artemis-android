package de.tum.informatics.www1.artemis.native_app.core.device

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

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

// Extensions
suspend fun NetworkStatusProvider.awaitInternetConnection() {
    currentNetworkStatus
        .filter { it is NetworkStatusProvider.NetworkStatus.Internet }
        .first()
}