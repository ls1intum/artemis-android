package de.tum.informatics.www1.artemis.native_app.feature.push.ui

import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.ui.ReloadableViewModel
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationConfigurationService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class PushNotificationSettingsViewModel internal constructor(
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
    private val pushNotificationConfigurationService: PushNotificationConfigurationService,
    private val coroutineContext: CoroutineContext = EmptyCoroutineContext
) : ReloadableViewModel() {

    companion object {
        private const val TAG = "PushNotificationSettingsViewModel"
    }

    internal val arePushNotificationsEnabled: StateFlow<Boolean> =
        serverConfigurationService
            .serverUrl
            .flatMapLatest { serverUrl ->
                pushNotificationConfigurationService.getArePushNotificationsEnabledFlow(serverUrl)
            }
            .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, false)

    internal fun updatePushNotificationEnabled(
        areEnabled: Boolean,
        onDone: (successful: Boolean) -> Unit
    ): Job {
        return viewModelScope.launch(coroutineContext) {
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
}
