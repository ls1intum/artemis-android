package de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.ui.settings

import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.onFailure
import de.tum.informatics.www1.artemis.native_app.core.data.onSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.ReloadableViewModel
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.course_notification_model.CourseNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.model.NotificationChannel
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.model.NotificationSettings
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.model.NotificationSettingsInfo
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.service.CourseNotificationSettingsService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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

    private val localChanges =
        MutableStateFlow<Map<String, Map<NotificationChannel, Boolean>>>(emptyMap())

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

    val combinedState: StateFlow<DataState<Pair<NotificationSettingsInfo, NotificationSettings>>> =
        combine(settingsInfoState, settingsState) { infoState, settingsState ->
            when {
                infoState is DataState.Success && settingsState is DataState.Success ->
                    DataState.Success(infoState.data to settingsState.data)

                infoState is DataState.Failure -> DataState.Failure(infoState.throwable)
                settingsState is DataState.Failure -> DataState.Failure(settingsState.throwable)

                infoState is DataState.Loading || settingsState is DataState.Loading ->
                    DataState.Loading()

                else -> DataState.Loading()
            }
        }.stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, DataState.Loading())

    fun selectPreset(presetId: Int) {
        viewModelScope.launch {
            val serverUrl = serverConfigurationService.serverUrl.first()
            val authToken = accountService.authToken.first()
            val info = settingsInfoState.first()
            val settings = settingsState.first()

            if (info is DataState.Success && settings is DataState.Success) {
                localChanges.update { emptyMap() }
                courseNotificationSettingsService
                    .selectPreset(courseId, presetId, serverUrl, authToken)
                onRequestReload()
            }
        }
    }

    val currentSettings: StateFlow<List<Pair<CourseNotificationType, Map<NotificationChannel, Boolean>>>> =
        combine(
            settingsInfoState.filterIsInstance<DataState.Success<NotificationSettingsInfo>>(),
            settingsState.filterIsInstance<DataState.Success<NotificationSettings>>(),
            localChanges
        ) { info, settings, local ->
            val merged = settings.data.notificationTypeChannels.toMutableMap()
            local.forEach { (k, v) -> merged[k] = v }

            val types = info.data.notificationTypes
            merged
                .toList()
                .sortedBy { (k, _) -> k.toIntOrNull() ?: 0 }
                .map { (k, v) -> (types[k] ?: CourseNotificationType.UNKNOWN) to v }
        }
            .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, emptyList())

    fun updateNotificationSetting(
        type: CourseNotificationType,
        enabled: Boolean
    ) = viewModelScope.launch {
        val (serverUrl, authToken) = serverConfigurationService.serverUrl.first() to
                accountService.authToken.first()
        val info = settingsInfoState.first() as? DataState.Success ?: return@launch
        val settings = settingsState.first() as? DataState.Success ?: return@launch

        val typeNumber = info.data.notificationTypes
            .entries.firstOrNull { it.value == type }?.key ?: return@launch

        val newChannels =
            (settings.data.notificationTypeChannels[typeNumber] ?: emptyMap())
                .toMutableMap()
                .apply { this[NotificationChannel.PUSH] = enabled }

        localChanges.update { it + (typeNumber to newChannels) }

        val setting = NotificationSettings(
            selectedPreset = settings.data.selectedPreset,
            notificationTypeChannels = settings.data.notificationTypeChannels + (typeNumber to newChannels)
        )
        courseNotificationSettingsService
            .updateSetting(courseId, setting, serverUrl, authToken)
            .onSuccess {
                onRequestReload()
            }
            .onFailure {
                localChanges.update { it - typeNumber }
            }


    }
}