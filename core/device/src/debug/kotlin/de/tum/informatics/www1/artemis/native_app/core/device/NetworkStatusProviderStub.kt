package de.tum.informatics.www1.artemis.native_app.core.device

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class NetworkStatusProviderStub(
    override val currentNetworkStatus: Flow<NetworkStatusProvider.NetworkStatus> = flowOf(
        NetworkStatusProvider.NetworkStatus.Internet
    )
) : NetworkStatusProvider