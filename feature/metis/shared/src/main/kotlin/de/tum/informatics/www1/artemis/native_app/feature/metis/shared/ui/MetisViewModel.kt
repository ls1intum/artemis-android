package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.common.withPrevious
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternetIndefinetly
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.AccountDataService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.websocket.WebsocketProvider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext

/**
 * Base view model which handles logic such as creating posts and reactions.
 */
abstract class MetisViewModel(
    serverConfigurationService: ServerConfigurationService,
    accountService: AccountService,
    private val accountDataService: AccountDataService,
    private val networkStatusProvider: NetworkStatusProvider,
    websocketProvider: WebsocketProvider,
    private val coroutineContext: CoroutineContext
) : ViewModel() {

    protected val onRequestReload = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    // Emits when a reload is manually requested or when we have a websocket reconnect
    val onReloadRequestAndWebsocketReconnect = merge(
        onRequestReload,
        websocketProvider
            .connectionState
            .withPrevious()
            .filter { (prevConnection, nowConnection) ->
                prevConnection != null && !prevConnection.isConnected && nowConnection.isConnected
            }
            .map { } // Convert to Unit
    )
        .shareIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, replay = 0)
        .onStart { emit(Unit) }

    val clientId: StateFlow<DataState<Long>> = flatMapLatest(
        accountDataService.onReloadRequired,
        onRequestReload.onStart { emit(Unit) }
    ) { _, _ ->
        retryOnInternetIndefinetly(
            networkStatusProvider.currentNetworkStatus
        ) {
            accountDataService.getAccountData().bind { it.id }
        }
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Lazily, DataState.Loading())

    val clientIdOrDefault: StateFlow<Long> = clientId
        .map { it.orElse(0L) }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Lazily, 0L)

    open fun requestReload() {
        onRequestReload.tryEmit(Unit)
    }
}
