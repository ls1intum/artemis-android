package de.tum.informatics.www1.artemis.native_app.service.impl.courses

import de.tum.informatics.www1.artemis.native_app.content.Course
import de.tum.informatics.www1.artemis.native_app.content.Dashboard
import de.tum.informatics.www1.artemis.native_app.service.AccountService
import de.tum.informatics.www1.artemis.native_app.service.DashboardService
import de.tum.informatics.www1.artemis.native_app.service.impl.KtorProvider
import de.tum.informatics.www1.artemis.native_app.service.impl.ServerCommunicationProvider
import de.tum.informatics.www1.artemis.native_app.util.DataState
import de.tum.informatics.www1.artemis.native_app.util.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.util.performNetworkCall
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.*

/**
 * [DashboardService] implementation that requests the dashboard from the Artemis server.
 */
class DashboardServiceImpl(
    private val ktorProvider: KtorProvider,
) : DashboardService {

    override suspend fun loadDashboard(
        authenticationData: AccountService.AuthenticationData.LoggedIn,
        serverUrl: String
    ): NetworkResponse<Dashboard> {
        //Perform a network call to $serverUrl/api/courses/for-dashboard
        return performNetworkCall {
            val courses: List<Course> = ktorProvider.ktorClient.get(serverUrl) {
                url {
                    appendPathSegments("api", "courses", "for-dashboard")
                }
                header("Authorization", authenticationData.asBearer)
            }.body() //Decode JSON to List<Course>

            Dashboard(courses)
        }
    }
}