package de.tum.informatics.www1.artemis.native_app.ui.courses_overview

import com.arkivanov.decompose.ComponentContext
import de.tum.informatics.www1.artemis.native_app.content.Dashboard
import de.tum.informatics.www1.artemis.native_app.service.AccountService
import de.tum.informatics.www1.artemis.native_app.service.DashboardService
import de.tum.informatics.www1.artemis.native_app.service.impl.ServerCommunicationProvider
import de.tum.informatics.www1.artemis.native_app.util.DataState
import de.tum.informatics.www1.artemis.native_app.util.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.util.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/**
 * The controller (ViewModel) of the courses overview screen.
 */
class CoursesOverviewComponent(
    componentContext: ComponentContext,
    private val onLogout: () -> Unit
) :
    ComponentContext by componentContext, KoinComponent {

    private val lifecycleScope: CoroutineScope = lifecycleScope()

    private val dashboardService: DashboardService = get()

    private val serverCommunicationProvider: ServerCommunicationProvider = get()

    private val accountService: AccountService = get()

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
                emit(DataState.Loading())
                //Called every time the authentication data changes, the server url changes or a reload is requested.
                val loadedDashboard = when (authenticationData) {
                    is AccountService.AuthenticationData.LoggedIn -> {
                        dashboardService.loadDashboard(authenticationData, serverUrl)
                    }
                    AccountService.AuthenticationData.NotLoggedIn -> {
                        //User is not logged in, nothing to fetch.
                        NetworkResponse.Response(Dashboard(emptyList()))
                    }
                }

                emit(DataState.Done(loadedDashboard))
            }
            //Store the loaded dashboard, so it is not loaded again when somebody collects this flow.
            .stateIn(lifecycleScope, SharingStarted.Lazily, DataState.Loading())

    /**
     * The client needs access to this url, to load the course icon.
     * The serverUrl comes without a trailing /
     */
    val serverUrl = serverCommunicationProvider.serverUrl
        .map { it.substring(0, it.length - 1) } //Remove the /
        .stateIn(lifecycleScope, SharingStarted.Eagerly, "")

    /**
     * Emits the current authentication bearer in the form: "Bearer $token"
     */
    val authorizationBearerToken = accountService.authenticationData.map { authenticationData ->
        when (authenticationData) {
            is AccountService.AuthenticationData.LoggedIn -> authenticationData.asBearer
            AccountService.AuthenticationData.NotLoggedIn -> ""
        }
    }.stateIn(lifecycleScope, SharingStarted.Eagerly, "")

    /**
     * Request a reload of the dashboard.
     */
    suspend fun reloadDashboard() {
        reloadDashboard.emit(Unit)
    }

    fun logout() {
        lifecycleScope.launch {
            accountService.logout()

            onLogout()
        }
    }
}