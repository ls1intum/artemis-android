package de.tum.informatics.www1.artemis.native_app.feature.push.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.transformLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationConfigurationService
import de.tum.informatics.www1.artemis.native_app.feature.push.ui.model.PushNotificationSetting
import de.tum.informatics.www1.artemis.native_app.feature.push.ui.model.group
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch

class PushNotificationSettingsViewModel internal constructor(
    private val notificationSettingsService: de.tum.informatics.www1.artemis.native_app.feature.push.service.NotificationSettingsService,
    networkStatusProvider: NetworkStatusProvider,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
    private val pushNotificationConfigurationService: PushNotificationConfigurationService
) : ViewModel() {

    internal val arePushNotificationsEnabled: StateFlow<Boolean> =
        serverConfigurationService
            .serverUrl
            .flatMapLatest { serverUrl ->
                pushNotificationConfigurationService.getArePushNotificationsEnabledFlow(serverUrl)
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)


    private val requestReloadSettings = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val loadedSettings: StateFlow<DataState<List<PushNotificationSetting>>> =
        transformLatest(
            requestReloadSettings.onStart { emit(Unit) },
            serverConfigurationService.serverUrl,
            accountService.authToken
        ) { _, serverUrl, authToken ->
            emitAll(
                retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                    notificationSettingsService.getNotificationSettings(
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

    internal fun updateSettingsEntry(
        settingsId: String,
        email: Boolean?,
        webapp: Boolean?,
        push: Boolean?
    ) {
        val newSettings = UpdatedSetting(email = email, webapp = webapp, push = push)
        val loadedSettings = loadedSettings.value

        if (loadedSettings is DataState.Success) {
            val syncedSettings = loadedSettings.data.applyChanges(syncedSettings.value)

            // The values set in the synced state
            val syncedSettingsValues =
                syncedSettings.firstOrNull { it.settingId == settingsId }?.let {
                    UpdatedSetting(email = it.email, webapp = it.webapp, push = it.push)
                } ?: UpdatedSetting(null, null, null)

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

    internal fun updatePushNotificationEnabled(
        areEnabled: Boolean,
        onDone: (successful: Boolean) -> Unit
    ): Job {
        return viewModelScope.launch {
            val serverUrl = serverConfigurationService.serverUrl.first()
            val authToken = accountService.authToken.first()

            onDone(
                pushNotificationConfigurationService.updateArePushNotificationEnabled(
                    newIsEnabled = areEnabled,
                    serverUrl = serverUrl,
                    authToken = authToken
                )
            )
        }
    }

    fun requestReloadSettings() {
        requestReloadSettings.tryEmit(Unit)
    }

    fun saveSettings(): Deferred<Boolean> {
        return viewModelScope.async {
            val settingsToSync = currentSettings.value
            if (settingsToSync !is DataState.Success) {
                return@async false
            }

            val response = notificationSettingsService.updateNotificationSettings(
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

            response is NetworkResponse.Response
        }
    }

    private fun List<PushNotificationSetting>.applyChanges(changes: Map<String, UpdatedSetting>): List<PushNotificationSetting> {
        return map { setting ->
            val updatedSettingsEntry = changes[setting.settingId] ?: return@map setting
            setting.copy(
                webapp = updatedSettingsEntry.webapp,
                email = updatedSettingsEntry.email,
                push = updatedSettingsEntry.push
            )
        }
    }

    private data class UpdatedSetting(val email: Boolean?, val webapp: Boolean?, val push: Boolean?)

    internal data class NotificationCategory(
        val categoryId: String,
        val settings: List<PushNotificationSetting>
    )
}