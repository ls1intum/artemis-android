package de.tum.informatics.www1.artemis.native_app.feature.dashboard.service.impl

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.model.Dashboard
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.service.DashboardService
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.appendPathSegments

/**
 * [DashboardService] implementation that requests the dashboard from the Artemis server.
 */
internal class DashboardServiceImpl(
    private val ktorProvider: KtorProvider
) : DashboardService {

    override suspend fun loadDashboard(
        authToken: String,
        serverUrl: String
    ): NetworkResponse<Dashboard> {
        return performNetworkCall {
            //Perform a network call to $serverUrl/api/courses/for-dashboard
            val dashboard: Dashboard = ktorProvider.ktorClient.get(serverUrl) {
                url {
                    appendPathSegments("api", "courses", "for-dashboard")
                }
                cookieAuth(authToken)
            }.body() //Decode JSON to List<Course>

            dashboard
        }
    }
}
