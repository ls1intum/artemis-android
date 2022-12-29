package de.tum.informatics.www1.artemis.native_app.core.push_notification_settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.transformLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.PushNotificationConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.push_notification_settings.model.PushNotificationSetting
import de.tum.informatics.www1.artemis.native_app.core.push_notification_settings.model.group
import de.tum.informatics.www1.artemis.native_app.core.push_notification_settings.service.PushNotificationSettingsService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PushNotificationSettingsViewModel(
    private val pushNotificationSettingsService: PushNotificationSettingsService,
    networkStatusProvider: NetworkStatusProvider,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
    private val pushNotificationConfigurationService: PushNotificationConfigurationService
) : ViewModel() {

    val arePushNotificationsEnabled: Flow<Boolean> = pushNotificationConfigurationService.arePushNotificationEnabled

    private val requestReloadSettings = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val loadedSettings: Flow<DataState<List<PushNotificationSetting>>> =
        transformLatest(
            requestReloadSettings.onStart { emit(Unit) },
            serverConfigurationService.serverUrl,
            accountService.authToken
        ) { _, serverUrl, authToken ->
            emitAll(
                retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                    pushNotificationSettingsService.getNotificationSettings(
                        serverUrl,
                        authToken
                    )
                }
            )
        }
            .stateIn(viewModelScope, SharingStarted.Eagerly, DataState.Loading())

    /**
     * Holds an entry for each updated setting
     */
    private val updatedSettings = MutableStateFlow<Map<String, UpdatedSettings>>(emptyMap())

    /**
     * Holds the settings with the changes the user made.
     */
    private val currentSettings: StateFlow<DataState<List<PushNotificationSetting>>> =
        combine(
            loadedSettings,
            updatedSettings
        ) { loadedSettings, updatedSettings ->
            loadedSettings.bind { settings ->
                settings.map { setting ->
                    val updatedSettingsEntry =
                        updatedSettings[setting.settingId] ?: return@map setting
                    setting.copy(
                        webapp = updatedSettingsEntry.webapp,
                        email = updatedSettingsEntry.email
                    )
                }
            }
        }
            .stateIn(viewModelScope, SharingStarted.Eagerly, DataState.Loading())

    val currentSettingsByGroup: StateFlow<DataState<Map<String, List<PushNotificationSetting>>>> = currentSettings
        .map { currentSettings ->
            currentSettings.bind { settings ->
                settings.groupBy { it.group }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, DataState.Loading())

    fun updateSettingsEntry(settingsId: String, email: Boolean?, webapp: Boolean?) {
        val newUpdatedSettings = updatedSettings.value.toMutableMap()
        newUpdatedSettings[settingsId] = UpdatedSettings(email = email, webapp = webapp)

        updatedSettings.value = newUpdatedSettings
    }

    fun updatePushNotificationEnabled(areEnabled: Boolean) {
        // TODO: Sync with server
        viewModelScope.launch {
            pushNotificationConfigurationService.updateArePushNotificationEnabled(areEnabled)
        }
    }

    fun requestReloadSettings() {
        requestReloadSettings.tryEmit(Unit)
    }

    private data class UpdatedSettings(val email: Boolean?, val webapp: Boolean?)
}