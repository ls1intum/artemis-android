package de.tum.informatics.www1.artemis.native_app.service

import de.tum.informatics.www1.artemis.native_app.content.Dashboard
import de.tum.informatics.www1.artemis.native_app.util.DataState
import de.tum.informatics.www1.artemis.native_app.util.NetworkResponse
import kotlinx.coroutines.flow.Flow

/**
 * Service where you can make requests about the Artemis dashboard.
 */
interface DashboardService {

    /**
     * Load the dashboard from the specified server using the specified authentication data.
     */
    suspend fun loadDashboard(authenticationData: AccountService.AuthenticationData.LoggedIn, serverUrl: String): NetworkResponse<Dashboard>
}