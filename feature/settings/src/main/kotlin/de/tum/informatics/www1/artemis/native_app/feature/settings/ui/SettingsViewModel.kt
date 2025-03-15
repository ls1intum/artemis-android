package de.tum.informatics.www1.artemis.native_app.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.app_version.AppVersionProvider
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.AccountDataService
import de.tum.informatics.www1.artemis.native_app.core.data.service.performAutoReloadingNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.isLoggedIn
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.account.Account
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationConfigurationService
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationJobService
import de.tum.informatics.www1.artemis.native_app.feature.push.unsubscribeFromNotifications
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class SettingsViewModel(
    private val accountService: AccountService,
    accountDataService: AccountDataService,
    networkStatusProvider: NetworkStatusProvider,
    private val pushNotificationJobService: PushNotificationJobService,
    private val pushNotificationConfigurationService: PushNotificationConfigurationService,
    appVersionProvider: AppVersionProvider,
    private val coroutineContext: CoroutineContext = EmptyCoroutineContext
) : ViewModel() {
    private val onRequestReload = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    val appVersion = appVersionProvider.appVersion

    val isLoggedIn: StateFlow<Boolean> = accountService.authenticationData.map { it.isLoggedIn }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, false)

    val account: StateFlow<DataState<Account>> =
        accountDataService.performAutoReloadingNetworkCall(
            networkStatusProvider = networkStatusProvider,
            manualReloadFlow = onRequestReload
        ) {
            getAccountData()
        }
            .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)


    fun onRequestLogout() {
        viewModelScope.launch(coroutineContext) {
            // the user manually logs out. Therefore we need to tell the server asap.
            unsubscribeFromNotifications(
                pushNotificationConfigurationService,
                pushNotificationJobService
            )

            accountService.logout()
        }
    }

    fun requestReload() {
        viewModelScope.launch(coroutineContext) {
            onRequestReload.emit(Unit)
        }
    }
}