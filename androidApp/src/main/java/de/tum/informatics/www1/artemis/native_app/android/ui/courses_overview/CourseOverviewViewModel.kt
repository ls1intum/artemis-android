package de.tum.informatics.www1.artemis.native_app.android.ui.courses_overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.android.content.Dashboard
import de.tum.informatics.www1.artemis.native_app.android.service.AccountService
import de.tum.informatics.www1.artemis.native_app.android.service.DashboardService
import de.tum.informatics.www1.artemis.native_app.android.service.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.android.service.ServerCommunicationProvider
import de.tum.informatics.www1.artemis.native_app.android.service.impl.ServerCommunicationProviderImpl
import de.tum.informatics.www1.artemis.native_app.android.util.DataState
import de.tum.informatics.www1.artemis.native_app.android.util.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.android.util.retryOnInternet
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * View model that fetches the dashboard from the model and supports reloading of the dashboard.
 */
class CourseOverviewViewModel(
    private val dashboardService: DashboardService,
    private val accountService: AccountService,
    private val networkStatusProvider: NetworkStatusProvider,
    serverCommunicationProvider: ServerCommunicationProvider
) : ViewModel() {

    /**
     * Emit a unit to this flow, to reload the dashboard.
     */
    private val reloadDashboard = MutableSharedFlow<Unit>()

    /**
     * Always emits the latest dashboard. Automatically updated when [reloadDashboard] is requested,
     * the login status changes or the server is updated.
     */
    val dashboard: StateFlow<DataState<Dashboard>> =
        combineTransform(
            accountService.authenticationData,
            serverCommunicationProvider.serverUrl,
            reloadDashboard.onStart { emit(Unit) }
        ) { authenticationData, serverUrl, _ ->
            emit(authenticationData to serverUrl)
        }
            .transformLatest { (authenticationData, serverUrl) ->
                //Called every time the authentication data changes, the server url changes or a reload is requested.
                when (authenticationData) {
                    is AccountService.AuthenticationData.LoggedIn -> {
                        emitAll(retryOnInternet(networkStatusProvider.currentNetworkStatus, retry = reloadDashboard) {
                            dashboardService.loadDashboard(authenticationData, serverUrl)
                        })
                    }
                    AccountService.AuthenticationData.NotLoggedIn -> {
                        //User is not logged in, nothing to fetch.
                        emit(DataState.Suspended())
                    }
                }
            }
            //Store the loaded dashboard, so it is not loaded again when somebody collects this flow.
            .stateIn(viewModelScope, SharingStarted.Lazily, DataState.Loading())

    /**
     * The client needs access to this url, to load the course icon.
     * The serverUrl comes without a trailing /
     */
    val serverUrl = serverCommunicationProvider.serverUrl
        .map { it.substring(0, it.length - 1) } //Remove the /
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    /**
     * Emits the current authentication bearer in the form: "Bearer $token"
     */
    val authorizationBearerToken = accountService.authenticationData.map { authenticationData ->
        when (authenticationData) {
            is AccountService.AuthenticationData.LoggedIn -> authenticationData.asBearer
            AccountService.AuthenticationData.NotLoggedIn -> ""
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, "")

    /**
     * Request a reload of the dashboard.
     */
    fun reloadDashboard() {
        viewModelScope.launch {
            reloadDashboard.emit(Unit)
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            accountService.logout()

            onDone()
        }
    }
}