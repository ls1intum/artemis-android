package de.tum.informatics.www1.artemis.native_app.core.push_notification_settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.transformLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.PushNotificationConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.push_notification_settings.model.PushNotificationSetting
import de.tum.informatics.www1.artemis.native_app.core.push_notification_settings.model.group
import de.tum.informatics.www1.artemis.native_app.core.push_notification_settings.service.SettingsService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PushNotificationSettingsViewModel internal constructor(
    private val settingsService: SettingsService,
    networkStatusProvider: NetworkStatusProvider,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
    private val pushNotificationConfigurationService: PushNotificationConfigurationService
) : ViewModel() {

    internal val arePushNotificationsEnabled: Flow<Boolean> =
        pushNotificationConfigurationService.arePushNotificationEnabled

    private val requestReloadSettings = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val loadedSettings: StateFlow<DataState<List<PushNotificationSetting>>> =
        transformLatest(
            requestReloadSettings.onStart { emit(Unit) },
            serverConfigurationService.serverUrl,
            accountService.authToken
        ) { _, serverUrl, authToken ->
            emitAll(
                retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                    settingsService.getNotificationSettings(
                        serverUrl,
                        authToken
                    )
                }
            )
        }
            .stateIn(viewModelScope, SharingStarted.Eagerly, DataState.Loading())

    /**
     * Holds an entry for each updated setting (not synced with the server)
     */
    private val updatedSettings = MutableStateFlow<Map<String, UpdatedSetting>>(emptyMap())

    /**
     * Changes made before that are already synced with the server
     */
    private val syncedSettings = MutableStateFlow<Map<String, UpdatedSetting>>(emptyMap())

    /**
     * Holds the settings with the changes the user made.
     */
    private val currentSettings: StateFlow<DataState<List<PushNotificationSetting>>> =
        combine(
            loadedSettings,
            updatedSettings,
            syncedSettings
        ) { loadedSettings, updatedSettings, syncedSettings ->
            loadedSettings.bind { settings ->
                settings.applyChanges(syncedSettings + updatedSettings)
            }
        }
            .stateIn(viewModelScope, SharingStarted.Eagerly, DataState.Loading())

    internal val currentSettingsByGroup: StateFlow<DataState<List<NotificationCategory>>> =
        currentSettings
            .map { currentSettings ->
                currentSettings.bind { settings ->
                    settings
                        .groupBy { it.group }
                        .map { NotificationCategory(it.key, it.value) }
                        .sortedBy { it.categoryId }
                }
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, DataState.Loading())

    /**
     * If there are unsynced changes with the server
     */
    val isDirty: Flow<Boolean> = updatedSettings.map { it.isNotEmpty() }

    internal fun updateSettingsEntry(settingsId: String, email: Boolean?, webapp: Boolean?) {
        val newSettings = UpdatedSetting(email = email, webapp = webapp)
        val loadedSettings = loadedSettings.value

        if (loadedSettings is DataState.Success) {
            val syncedSettings = loadedSettings.data.applyChanges(syncedSettings.value)

            // The values set in the synced state
            val syncedSettingsValues =
                syncedSettings.firstOrNull { it.settingId == settingsId }?.let {
                    UpdatedSetting(email = it.email, webapp = it.webapp)
                } ?: UpdatedSetting(null, null)

            val newUpdatedSettings = updatedSettings.value.toMutableMap()
            if (syncedSettingsValues != newSettings) {
                newUpdatedSettings[settingsId] = newSettings
            } else {
                // Settings are back to default, therefore we can remove the value
                newUpdatedSettings.remove(settingsId)
            }

            updatedSettings.value = newUpdatedSettings
        }
    }

    internal fun updatePushNotificationEnabled(areEnabled: Boolean) {
        // TODO: Sync with server
        viewModelScope.launch {
            pushNotificationConfigurationService.updateArePushNotificationEnabled(areEnabled)
        }
    }

    fun requestReloadSettings() {
        requestReloadSettings.tryEmit(Unit)
    }

    fun saveSettings(onResponse: (successful: Boolean) -> Unit): Job {
        return viewModelScope.launch {
            val settingsToSync = currentSettings.value
            if (settingsToSync !is DataState.Success) {
                onResponse(false)
                return@launch
            }

            val response = settingsService.updateNotificationSettings(
                settingsToSync.data,
                serverConfigurationService.serverUrl.first(),
                accountService.authToken.first()
            )

            if (response is NetworkResponse.Response) {
                val newSyncedSettings = syncedSettings.value.toMutableMap()
                newSyncedSettings += updatedSettings.value
                syncedSettings.value = newSyncedSettings
                updatedSettings.value = emptyMap()
            }

            onResponse(response is NetworkResponse.Response)
        }
    }

    private fun List<PushNotificationSetting>.applyChanges(changes: Map<String, UpdatedSetting>): List<PushNotificationSetting> {
        return map { setting ->
            val updatedSettingsEntry = changes[setting.settingId] ?: return@map setting
            setting.copy(
                webapp = updatedSettingsEntry.webapp,
                email = updatedSettingsEntry.email
            )
        }
    }

    private data class UpdatedSetting(val email: Boolean?, val webapp: Boolean?)

    internal data class NotificationCategory(val categoryId: String, val settings: List<PushNotificationSetting>)
}