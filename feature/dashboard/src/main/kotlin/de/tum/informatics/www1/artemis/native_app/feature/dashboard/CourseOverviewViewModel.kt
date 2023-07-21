package de.tum.informatics.www1.artemis.native_app.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.Dashboard
import de.tum.informatics.www1.artemis.native_app.core.ui.authTokenStateFlow
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.service.DashboardService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * View model that fetches the dashboard from the model and supports reloading of the dashboard.
 */
internal class CourseOverviewViewModel(
    private val dashboardService: DashboardService,
    private val accountService: AccountService,
    serverConfigurationService: ServerConfigurationService,
    networkStatusProvider: NetworkStatusProvider,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
) : ViewModel() {

    /**
     * Emit a unit to this flow, to reload the dashboard.
     */
    private val reloadDashboard = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /**
     * Always emits the latest dashboard. Automatically updated when [requestReloadDashboard] is requested,
     * the login status changes or the server is updated.
     */
    val dashboard: StateFlow<DataState<Dashboard>> =
        flatMapLatest(
            accountService.authToken,
            serverConfigurationService.serverUrl,
            reloadDashboard.onStart { emit(Unit) }
        ) { authToken, serverUrl, _ ->
            retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                dashboardService.loadDashboard(
                    authToken,
                    serverUrl
                )
            }
        }
            //Store the loaded dashboard, so it is not loaded again when somebody collects this flow.
            .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, DataState.Loading())

    /**
     * The client needs access to this url, to load the course icon.
     * The serverUrl comes without a trailing /
     */
    val serverUrl: StateFlow<String> = serverConfigurationService.serverUrl
        .map { it.dropLast(1) } //Remove the /
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, "")

    /**
     * Emits the current authentication bearer in the form: "Bearer $token"
     */
    val authToken: StateFlow<String> = authTokenStateFlow(accountService)

    /**
     * Request a reload of the dashboard.
     */
    fun requestReloadDashboard() {
        reloadDashboard.tryEmit(Unit)
    }
}
