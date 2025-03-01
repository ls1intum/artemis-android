package de.tum.informatics.www1.artemis.native_app.feature.dashboard.service.impl

import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.ApiEndpoint
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.ArtemisContextBasedServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.model.Dashboard
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.service.DashboardService
import io.ktor.http.appendPathSegments

/**
 * [DashboardService] implementation that requests the dashboard from the Artemis server.
 */
internal class DashboardServiceImpl(
    ktorProvider: KtorProvider,
    artemisContextProvider: ArtemisContextProvider,
) : ArtemisContextBasedServiceImpl(ktorProvider, artemisContextProvider), DashboardService {

    override suspend fun loadDashboard(): NetworkResponse<Dashboard> {
        return getRequest {
            url {
                appendPathSegments(*ApiEndpoint.core_courses, "for-dashboard")
            }
        }
    }
}
