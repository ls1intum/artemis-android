package de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context

import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.onStart

interface ArtemisContextBasedService<T: ArtemisContext> {
    val onArtemisContextChanged: Flow<T>
}

/**
 * Performs a network call with automatic reloading. The reload happens when the artemis context for
 * this service changed or when the manualReloadFlow emits.
 *
 * @param T The type of the response data.
 * @param S The service type which must implement `ArtemisContextBasedService`.
 * @param networkStatusProvider Provides the current network status.
 * @param manualReloadFlow A flow that triggers manual reloads. Defaults to an empty flow.
 * @param serviceCall A suspend function representing the network call to be performed.
 * @return A flow emitting `DataState` representing the state of the network call.
 */
fun <T : Any, S, AC> S.performAutoReloadingNetworkCall(
    networkStatusProvider: NetworkStatusProvider,
    manualReloadFlow: Flow<Unit> = emptyFlow(),
    serviceCall: suspend S.() -> NetworkResponse<T>
): Flow<DataState<T>> where S : ArtemisContextBasedService<AC> = flatMapLatest(
    this.onArtemisContextChanged,
    manualReloadFlow.onStart { emit(Unit) }
) { _, _ ->
    retryOnInternet(networkStatusProvider.currentNetworkStatus) {
        this.serviceCall()
    }
}