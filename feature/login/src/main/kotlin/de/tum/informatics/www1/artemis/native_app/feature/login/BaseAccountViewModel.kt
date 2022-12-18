package de.tum.informatics.www1.artemis.native_app.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.service.ServerDataService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.server_config.ProfileInfo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

abstract class BaseAccountViewModel(
    serverConfigurationService: ServerConfigurationService,
    networkStatusProvider: NetworkStatusProvider,
    serverDataService: ServerDataService
) : ViewModel() {

    val serverProfileInfo: StateFlow<DataState<ProfileInfo>> = serverConfigurationService
        .serverUrl
        .flatMapLatest { serverUrl ->
            retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                serverDataService.getServerProfileInfo(serverUrl)
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, DataState.Loading())
}