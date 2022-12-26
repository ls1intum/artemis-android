package de.tum.informatics.www1.artemis.native_app.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.model.Dashboard
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.service.DashboardService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * View model that fetches the dashboard from the model and supports reloading of the dashboard.
 */
internal class CourseOverviewViewModel(
    private val dashboardService: DashboardService,
    private val accountService: AccountService,
    serverConfigurationService: ServerConfigurationService
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
        combineTransform(
            accountService.authenticationData,
            serverConfigurationService.serverUrl,
            reloadDashboard.onStart { emit(Unit) }
        ) { authenticationData, serverUrl, _ ->
            emit(authenticationData to serverUrl)
        }
            .transformLatest { (authenticationData, serverUrl) ->
                //Called every time the authentication data changes, the server url changes or a reload is requested.
                when (authenticationData) {
                    is AccountService.AuthenticationData.LoggedIn -> {
                        emitAll(
                            dashboardService.loadDashboard(
                                authenticationData.authToken,
                                serverUrl
                            )
                        )
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
    val serverUrl = serverConfigurationService.serverUrl
        .map { it.dropLast(1) } //Remove the /
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
    fun requestReloadDashboard() {
        reloadDashboard.tryEmit(Unit)
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            accountService.logout()

            onDone()
        }
    }
}