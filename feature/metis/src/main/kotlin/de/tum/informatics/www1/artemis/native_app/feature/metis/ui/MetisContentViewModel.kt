package de.tum.informatics.www1.artemis.native_app.feature.metis.ui

import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.data.service.ServerDataService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.websocket.impl.WebsocketProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisModificationService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Common viewModel for metis viewModels that display live metis content.
 * Live metis content is content that is being permanently updated by websockets.
 */
abstract class MetisContentViewModel(
    private val websocketProvider: WebsocketProvider,
    metisModificationService: MetisModificationService,
    metisStorageService: MetisStorageService,
    serverConfigurationService: ServerConfigurationService,
    accountService: AccountService,
    serverDataService: ServerDataService,
    networkStatusProvider: NetworkStatusProvider
) : MetisViewModel(
    metisModificationService,
    metisStorageService,
    serverConfigurationService,
    accountService,
    serverDataService,
    networkStatusProvider
) {

    /**
     * Emits true if the data may be outdated. Listens to the connection state of the websocket
     * If a connection was established and is broken, then the data may be corrupted. A reload resets this
     */
    val isDataOutdated: StateFlow<Boolean> = onRequestReload
        .onStart { emit(Unit) }
        .transformLatest {
            emit(false)
            var wasConnected = false

            websocketProvider.connectionState.collect { connectionState ->
                when (connectionState) {
                    is WebsocketProvider.WebsocketConnectionState.WithSession -> {
                        if (!wasConnected && connectionState.isConnected) {
                            wasConnected = true
                        } else if (wasConnected && !connectionState.isConnected) {
                            emit(true)
                        }
                    }
                    WebsocketProvider.WebsocketConnectionState.Empty -> {
                        wasConnected = false
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /**
     * Emits to onRequestReload. If the websocket is currently not connected, requests a reconnect to the websocket
     */
    override fun requestReload() {
        super.requestReload()
        viewModelScope.launch {
            if (!websocketProvider.isConnected.first()) {
                websocketProvider.requestTryReconnect()
            }
        }
    }
}