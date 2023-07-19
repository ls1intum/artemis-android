package de.tum.informatics.www1.artemis.native_app.feature.course_registration.service.impl

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.feature.course_registration.service.CourseRegistrationService
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType

internal class CourseRegistrationServiceImpl(
    private val ktorProvider: KtorProvider
) : CourseRegistrationService {

    override suspend fun fetchRegistrableCourses(
        serverUrl: String,
        authToken: String
    ): NetworkResponse<List<Course>> {
        return performNetworkCall {
            ktorProvider.ktorClient
                .get(serverUrl) {
                    url {
                        appendPathSegments("api", "courses", "for-enrollment")
                    }

                    cookieAuth(authToken)
                    contentType(ContentType.Application.Json)
                }
                .body()
        }
    }

    override suspend fun registerInCourse(
        serverUrl: String,
        authToken: String,
        courseId: Long
    ): NetworkResponse<HttpStatusCode> {
        return performNetworkCall {
            ktorProvider.ktorClient
                .post(serverUrl) {
                    url {
                        appendPathSegments("api", "courses", courseId.toString(), "register")
                    }

                    cookieAuth(authToken)
                }.status
            // TODO: sync groups
        }
    }
}