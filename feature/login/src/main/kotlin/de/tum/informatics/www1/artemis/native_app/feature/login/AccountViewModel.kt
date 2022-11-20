package de.tum.informatics.www1.artemis.native_app.feature.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.datastore.defaults.ArtemisInstances
import de.tum.informatics.www1.artemis.native_app.core.model.server_config.ProfileInfo
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.service.ServerDataService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import de.tum.informatics.www1.artemis.native_app.feature.account.R

class AccountViewModel(
    private val serverConfigurationService: ServerConfigurationService,
    private val serverDataService: ServerDataService,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val selectedArtemisInstance: Flow<ArtemisInstances.ArtemisInstance> =
        serverConfigurationService.serverUrl.map { serverUrl ->
            val providedInstance =
                ArtemisInstances.instances.firstOrNull { it.serverUrl == serverUrl }

            providedInstance ?: ArtemisInstances.ArtemisInstance(
                serverUrl,
                R.string.artemis_instance_custom,
                ArtemisInstances.ArtemisInstance.Type.CUSTOM
            )
        }

    val serverProfileInfo: Flow<DataState<ProfileInfo>> =
        serverConfigurationService.serverUrl
            .transformLatest { serverUrl ->
                emitAll(
                    serverDataService.getServerProfileInfo(serverUrl)
                )
            }
            .transform { dataState ->
                emit(dataState)
                if (dataState is DataState.Loading) {
                    delay(200L)
                }
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, DataState.Loading())

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