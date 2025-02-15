package de.tum.informatics.www1.artemis.native_app.feature.dashboard.service

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.ArtemisContextBasedService
import de.tum.informatics.www1.artemis.native_app.core.model.Dashboard

/**
 * Service where you can make requests about the Artemis dashboard.
 */
interface DashboardService : ArtemisContextBasedService {

    suspend fun loadDashboard(): NetworkResponse<Dashboard>
}