package de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.ui.settings

import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.ReloadableViewModel
import de.tum.informatics.www1.artemis.native_app.core.ui.serverUrlStateFlow
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.course_notification_model.CourseNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.model.NotificationChannel
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.model.NotificationSettings
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.model.NotificationSettingsInfo
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.model.NotificationSettingsPreset
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.service.CourseNotificationSettingsService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal class CourseNotificationSettingsViewModel(
    private val courseId: Long,
    private val courseNotificationSettingsService: CourseNotificationSettingsService,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
    private val networkStatusProvider: NetworkStatusProvider,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
) : ReloadableViewModel() {

    val serverUrl: StateFlow<String> = serverUrlStateFlow(serverConfigurationService)

    private val settingsInfoState: StateFlow<DataState<NotificationSettingsInfo>> =
        combine(
            serverConfigurationService.serverUrl,
            accountService.authToken,
            requestReload.onStart { emit(Unit) }
        ) { serverUrl, authToken, _ -> serverUrl to authToken }
            .flatMapLatest { (serverUrl, authToken) ->
                retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                    courseNotificationSettingsService.getNotificationSettingsInfo(
                        serverUrl = serverUrl,
                        authToken = authToken
                    )
                }
            }
            .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, DataState.Loading())

    private val settingsState: StateFlow<DataState<NotificationSettings>> =
        combine(
            serverConfigurationService.serverUrl,
            accountService.authToken,
            requestReload.onStart { emit(Unit) }
        ) { serverUrl, authToken, _ -> serverUrl to authToken }
            .flatMapLatest { (serverUrl, authToken) ->
                retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                    courseNotificationSettingsService.getNotificationSettings(
                        courseId = courseId,
                        serverUrl = serverUrl,
                        authToken = authToken
                    )
                }
            }
            .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, DataState.Loading())

    val presets: StateFlow<List<NotificationSettingsPreset>> =
        settingsInfoState
            .filterIsInstance<DataState.Success<NotificationSettingsInfo>>()
            .map { it.data.presets }
            .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, emptyList())

    val currentPreset: StateFlow<Int> =
        settingsState
            .filterIsInstance<DataState.Success<NotificationSettings>>()
            .map { it.data.selectedPreset }
            .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, 0)

    val currentSettings: StateFlow<List<Pair<CourseNotificationType, Map<NotificationChannel, Boolean>>>> =
        combine(
            settingsInfoState.filterIsInstance<DataState.Success<NotificationSettingsInfo>>(),
            settingsState.filterIsInstance<DataState.Success<NotificationSettings>>()
        ) { info, settings ->
            val types = info.data.notificationTypes
            val sorted = settings.data.notificationTypeChannels.toList().sortedBy { (key, _) ->
                key.toIntOrNull() ?: 0
            }
            sorted.map { (key, value) ->
                val type = types[key] ?: CourseNotificationType.UNKNOWN
                type to value
            }
        }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, emptyList())

    fun updateNotificationSetting(
        type: CourseNotificationType,
        enabled: Boolean
    ) {
        viewModelScope.launch {
            val serverUrl = serverConfigurationService.serverUrl.first()
            val authToken = accountService.authToken.first()
            val info = settingsInfoState.first()
            val settings = settingsState.first()

            if (info is DataState.Success && settings is DataState.Success) {
                val typeNumber = info.data.notificationTypes.entries.firstOrNull { it.value.name == type.name }?.key ?: "0"
                val currentSettings = settings.data.notificationTypeChannels[typeNumber]?.toMutableMap() ?: mutableMapOf()
                currentSettings[NotificationChannel.PUSH] = enabled
                
                val updatedSettings = settings.data.notificationTypeChannels.toMutableMap()
                updatedSettings[typeNumber] = currentSettings

                courseNotificationSettingsService.updateSetting(
                    courseId = courseId,
                    typeNumber = typeNumber,
                    setting = currentSettings,
                    serverUrl = serverUrl,
                    authToken = authToken
                )

                onRequestReload()
            }
        }
    }

    fun selectPreset(presetId: Int) {
        viewModelScope.launch {
            val serverUrl = serverConfigurationService.serverUrl.first()
            val authToken = accountService.authToken.first()
            val info = settingsInfoState.first()
            val settings = settingsState.first()

            if (info is DataState.Success && settings is DataState.Success) {
                val newPreset = info.data.presets.firstOrNull { it.typeId == presetId }

                val newSettings = mutableMapOf<String, Map<NotificationChannel, Boolean>>()
                newPreset?.presetMap?.forEach { (type, value) ->
                    val number = info.data.notificationTypes.entries.firstOrNull { it.value == type }?.key ?: "0"
                    newSettings[number] = value
                }

                try {
                    courseNotificationSettingsService.selectPreset(
                        courseId = courseId,
                        presetId = presetId,
                        serverUrl = serverUrl,
                        authToken = authToken
                    )
                    onRequestReload()
                } catch (e: Exception) {
                    // display error message to user
                }
            }
        }
    }
} 