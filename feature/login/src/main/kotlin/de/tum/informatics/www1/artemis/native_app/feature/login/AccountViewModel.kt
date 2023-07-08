package de.tum.informatics.www1.artemis.native_app.feature.login

import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.defaults.ArtemisInstances
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.feature.login.service.ServerProfileInfoService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AccountViewModel(
    serverConfigurationService: ServerConfigurationService,
    serverProfileInfoService: ServerProfileInfoService,
    networkStatusProvider: NetworkStatusProvider
) : BaseAccountViewModel(serverConfigurationService, networkStatusProvider, serverProfileInfoService) {

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
            .stateIn(viewModelScope, SharingStarted.Eagerly, ArtemisInstances.TumArtemis)
}
