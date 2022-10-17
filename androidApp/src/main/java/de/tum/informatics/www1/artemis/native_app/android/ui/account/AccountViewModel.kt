package de.tum.informatics.www1.artemis.native_app.android.ui.account

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.android.R
import de.tum.informatics.www1.artemis.native_app.android.defaults.ArtemisInstances
import de.tum.informatics.www1.artemis.native_app.android.server_config.ProfileInfo
import de.tum.informatics.www1.artemis.native_app.android.service.ServerCommunicationProvider
import de.tum.informatics.www1.artemis.native_app.android.util.DataState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AccountViewModel(
    private val serverCommunicationProvider: ServerCommunicationProvider,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val selectedArtemisInstance: Flow<ArtemisInstances.ArtemisInstance> =
        serverCommunicationProvider.serverUrl.map { serverUrl ->
            val providedInstance =
                ArtemisInstances.instances.firstOrNull { it.serverUrl == serverUrl }

            providedInstance ?: ArtemisInstances.ArtemisInstance(
                serverUrl,
                R.string.artemis_instance_custom,
                ArtemisInstances.ArtemisInstance.Type.CUSTOM
            )
        }

    val serverProfileInfo: Flow<DataState<ProfileInfo>> =
        serverCommunicationProvider.serverProfileInfo
            .transform { dataState ->
                emit(dataState)
                if (dataState is DataState.Loading) {
                    delay(200L)
                }
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, DataState.Loading())

    fun updateServerUrl(serverUrl: String) {
        viewModelScope.launch {
            serverCommunicationProvider.updateServerUrl(serverUrl)
        }
    }

    fun retryLoadServerProfileInfo() {
        viewModelScope.launch {
            serverCommunicationProvider.retryLoadServerProfileInfo()
        }
    }
}