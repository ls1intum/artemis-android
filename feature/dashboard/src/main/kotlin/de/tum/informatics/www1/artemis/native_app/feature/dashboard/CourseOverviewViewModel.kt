package de.tum.informatics.www1.artemis.native_app.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.transformLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.service.DashboardService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.model.Dashboard
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

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
        transformLatest(
            accountService.authToken,
            serverConfigurationService.serverUrl,
            reloadDashboard.onStart { emit(Unit) }
        ) { authToken, serverUrl, _ ->
            //Called every time the authentication data changes, the server url changes or a reload is requested.
            emitAll(
                dashboardService.loadDashboard(
                    authToken,
                    serverUrl
                )
            )
        }
            //Store the loaded dashboard, so it is not loaded again when somebody collects this flow.
            .stateIn(viewModelScope, SharingStarted.Eagerly, DataState.Loading())

    /**
     * The client needs access to this url, to load the course icon.
     * The serverUrl comes without a trailing /
     */
    val serverUrl: StateFlow<String> = serverConfigurationService.serverUrl
        .map { it.dropLast(1) } //Remove the /
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    /**
     * Emits the current authentication bearer in the form: "Bearer $token"
     */
    val authorizationBearerToken: StateFlow<String> = accountService.authenticationData.map { authenticationData ->
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
}
