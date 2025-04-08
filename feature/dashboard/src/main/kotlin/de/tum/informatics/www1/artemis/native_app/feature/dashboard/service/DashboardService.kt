package de.tum.informatics.www1.artemis.native_app.feature.dashboard.service

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.LoggedInBasedService
import de.tum.informatics.www1.artemis.native_app.core.model.Dashboard

/**
 * Service where you can make requests about the Artemis dashboard.
 */
interface DashboardService : LoggedInBasedService {

    suspend fun loadDashboard(): NetworkResponse<Dashboard>
}