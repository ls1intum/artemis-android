package de.tum.informatics.www1.artemis.native_app.core.data.service.impl.courses

import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.service.DashboardService
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.CourseWithScore
import de.tum.informatics.www1.artemis.native_app.core.model.Dashboard
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.appendPathSegments
import kotlinx.coroutines.flow.Flow

/**
 * [DashboardService] implementation that requests the dashboard from the Artemis server.
 */
internal class DashboardServiceImpl(
    private val ktorProvider: KtorProvider,
    private val networkStatusProvider: NetworkStatusProvider
) : DashboardService {

    override suspend fun loadDashboard(
        authToken: String,
        serverUrl: String
    ): Flow<DataState<Dashboard>> {
        return retryOnInternet(networkStatusProvider.currentNetworkStatus) {
            performNetworkCall {
                //Perform a network call to $serverUrl/api/courses/for-dashboard
                val courses: List<CourseWithScore> = ktorProvider.ktorClient.get(serverUrl) {
                    url {
                        appendPathSegments("api", "courses", "for-dashboard")
                    }
                    cookieAuth(authToken)
                }.body() //Decode JSON to List<Course>

                Dashboard(courses)
            }
        }
    }
}