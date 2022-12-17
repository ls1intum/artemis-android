package de.tum.informatics.www1.artemis.native_app.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.datastore.defaults.ArtemisInstances
import de.tum.informatics.www1.artemis.native_app.core.model.server_config.ProfileInfo
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.service.ServerDataService
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import de.tum.informatics.www1.artemis.native_app.feature.account.R

class AccountViewModel(
    private val serverConfigurationService: ServerConfigurationService,
    private val serverDataService: ServerDataService,
    private val networkStatusProvider: NetworkStatusProvider
) : BaseAccountViewModel(serverConfigurationService, networkStatusProvider, serverDataService) {

    val selectedArtemisInstance: StateFlow<ArtemisInstances.ArtemisInstance> =
        serverConfigurationService.serverUrl.map { serverUrl ->
            val providedInstance =
                ArtemisInstances.instances.firstOrNull { it.serverUrl == serverUrl }

            providedInstance ?: ArtemisInstances.ArtemisInstance(
                serverUrl,
                R.string.artemis_instance_custom,
                ArtemisInstances.ArtemisInstance.Type.CUSTOM
            )
        }
            .stateIn(viewModelScope, SharingStarted.Eagerly, ArtemisInstances.TUM_ARTEMIS)

    fun updateServerUrl(serverUrl: String) {
        viewModelScope.launch {
            serverConfigurationService.updateServerUrl(serverUrl)
        }
    }

    fun retryLoadServerProfileInfo() {
        viewModelScope.launch {
            serverConfigurationService.retryLoadServerProfileInfo()
        }
    }
}