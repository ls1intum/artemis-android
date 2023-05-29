package de.tum.informatics.www1.artemis.native_app.feature.metis.ui

import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.service.ServerDataService
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.websocket.impl.WebsocketProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisModificationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.hasModerationRights
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.ConversationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.getConversation
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch

/**
 * Common viewModel for metis viewModels that display live metis content.
 * Live metis content is content that is being permanently updated by websockets.
 */
abstract class MetisContentViewModel(
    metisContext: MetisContext,
    private val websocketProvider: WebsocketProvider,
    metisModificationService: MetisModificationService,
    metisStorageService: MetisStorageService,
    serverConfigurationService: ServerConfigurationService,
    accountService: AccountService,
    serverDataService: ServerDataService,
    networkStatusProvider: NetworkStatusProvider,
    conversationService: ConversationService
) : MetisViewModel(
    metisModificationService,
    metisStorageService,
    serverConfigurationService,
    accountService,
    serverDataService,
    networkStatusProvider
) {

    val hasModerationRights: StateFlow<DataState<Boolean>> = when (metisContext) {
        is MetisContext.Conversation -> {
            flatMapLatest(
                serverConfigurationService.serverUrl,
                accountService.authToken,
                onRequestReload.onStart { emit(Unit) }
            ) { serverUrl, authToken, _ ->
                retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                    conversationService
                        .getConversation(
                            courseId = metisContext.courseId,
                            conversationId = metisContext.conversationId,
                            authToken = serverUrl,
                            serverUrl = authToken
                        )
                        .bind { it.hasModerationRights }
                }
            }
        }

        else -> flowOf(DataState.Success(false))
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly)

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