package de.tum.informatics.www1.artemis.native_app.android.service.impl.courses

import de.tum.informatics.www1.artemis.native_app.android.content.Course
import de.tum.informatics.www1.artemis.native_app.android.service.CourseService
import de.tum.informatics.www1.artemis.native_app.android.service.impl.KtorProvider
import de.tum.informatics.www1.artemis.native_app.android.util.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.android.util.performNetworkCall
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class CourseServiceImpl(private val ktorProvider: KtorProvider) : CourseService {

    override suspend fun getCourse(
        courseId: Int,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Course> {
        return performNetworkCall {
            ktorProvider.ktorClient.get(serverUrl) {
                url {
                    appendPathSegments("api", "courses", courseId.toString(), "for-dashboard")
                }

                contentType(ContentType.Application.Json)
                bearerAuth(authToken)
            }.body()
        }
    }
}