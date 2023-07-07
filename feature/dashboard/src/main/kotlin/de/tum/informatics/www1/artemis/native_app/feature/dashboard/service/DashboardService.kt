package de.tum.informatics.www1.artemis.native_app.feature.dashboard.service

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.model.Dashboard

/**
 * Service where you can make requests about the Artemis dashboard.
 */
interface DashboardService {

    /**
     * Load the dashboard from the specified server using the specified authentication data.
     */
    suspend fun loadDashboard(authToken: String, serverUrl: String): NetworkResponse<Dashboard>
}