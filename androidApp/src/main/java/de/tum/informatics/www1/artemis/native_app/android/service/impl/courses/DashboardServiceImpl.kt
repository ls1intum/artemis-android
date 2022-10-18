package de.tum.informatics.www1.artemis.native_app.android.service.impl.courses

import de.tum.informatics.www1.artemis.native_app.android.content.Course
import de.tum.informatics.www1.artemis.native_app.android.content.Dashboard
import de.tum.informatics.www1.artemis.native_app.android.service.DashboardService
import de.tum.informatics.www1.artemis.native_app.android.service.impl.KtorProvider
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * [DashboardService] implementation that requests the dashboard from the Artemis server.
 */
class DashboardServiceImpl(
    private val ktorProvider: KtorProvider,
) : DashboardService {

    override suspend fun loadDashboard(
        authToken: String,
        serverUrl: String
    ): Dashboard {
        //Perform a network call to $serverUrl/api/courses/for-dashboard
        val courses: List<Course> = ktorProvider.ktorClient.get(serverUrl) {
            url {
                appendPathSegments("api", "courses", "for-dashboard")
            }
            bearerAuth(authToken)
        }.body() //Decode JSON to List<Course>

        return Dashboard(courses)
    }
}