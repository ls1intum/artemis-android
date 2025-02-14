package de.tum.informatics.www1.artemis.native_app.core.data.service

import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.onStart

interface ArtemisContextBasedService {
    val onReloadRequired: Flow<Unit>
}

fun <T : Any, S> S.performAutoReloadingNetworkCall(
    networkStatusProvider: NetworkStatusProvider,
    manualReloadFlow: Flow<Unit> = emptyFlow(),
    serviceCall: suspend S.() -> NetworkResponse<T>
): Flow<DataState<T>> where S : ArtemisContextBasedService = flatMapLatest(
    this.onReloadRequired,
    manualReloadFlow.onStart { emit(Unit) }
) { _, _ ->
    retryOnInternet(networkStatusProvider.currentNetworkStatus) {
        this.serviceCall()
    }
}