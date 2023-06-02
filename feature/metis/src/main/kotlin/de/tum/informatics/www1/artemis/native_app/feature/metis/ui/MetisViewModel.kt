package de.tum.informatics.www1.artemis.native_app.feature.metis.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.common.withPrevious
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternetIndefinetly
import de.tum.informatics.www1.artemis.native_app.core.data.service.ServerDataService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.websocket.impl.WebsocketProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisModificationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.ConversationService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn

/**
 * Base view model which handles logic such as creating posts and reactions.
 */
abstract class MetisViewModel(
    serverConfigurationService: ServerConfigurationService,
    accountService: AccountService,
    private val serverDataService: ServerDataService,
    private val networkStatusProvider: NetworkStatusProvider,
    websocketProvider: WebsocketProvider
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
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 0)
        .onStart { emit(Unit) }

    val clientId: StateFlow<DataState<Long>> = flatMapLatest(
        serverConfigurationService.serverUrl,
        accountService.authToken,
        onRequestReload.onStart { emit(Unit) }
    ) { serverUrl, authToken, _ ->
        retryOnInternetIndefinetly(
            networkStatusProvider.currentNetworkStatus
        ) {
            serverDataService.getAccountData(serverUrl, authToken).bind { it.id }
        }
    }
        .stateIn(viewModelScope, SharingStarted.Lazily, DataState.Loading())

    val clientIdOrDefault: StateFlow<Long> = clientId
        .map { it.orElse(0L) }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0L)

    open fun requestReload() {
        onRequestReload.tryEmit(Unit)
    }
}
