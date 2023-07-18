package de.tum.informatics.www1.artemis.native_app.feature.course_registration.service.impl

import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow

internal class CourseRegistrationServiceImpl(
    private val ktorProvider: KtorProvider,
    private val networkStatusProvider: NetworkStatusProvider
) : de.tum.informatics.www1.artemis.native_app.feature.course_registration.service.CourseRegistrationService {

    override suspend fun fetchRegistrableCourses(
        serverUrl: String,
        authToken: String
    ): Flow<DataState<List<Course>>> {
        return retryOnInternet(networkStatusProvider.currentNetworkStatus) {
            performNetworkCall {
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