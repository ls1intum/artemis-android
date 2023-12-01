package de.tum.informatics.www1.artemis.native_app.device.test

import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class NetworkStatusProviderStub : NetworkStatusProvider {
    override val currentNetworkStatus: Flow<NetworkStatusProvider.NetworkStatus> =
        flowOf(NetworkStatusProvider.NetworkStatus.Internet)
}
