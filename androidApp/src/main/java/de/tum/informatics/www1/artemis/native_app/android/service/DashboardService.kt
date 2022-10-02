package de.tum.informatics.www1.artemis.native_app.android.service

import de.tum.informatics.www1.artemis.native_app.android.content.Dashboard

/**
 * Service where you can make requests about the Artemis dashboard.
 */
interface DashboardService {

    /**
     * Load the dashboard from the specified server using the specified authentication data.
     */
    suspend fun loadDashboard(authenticationData: AccountService.AuthenticationData.LoggedIn, serverUrl: String): Dashboard
}