package de.tum.informatics.www1.artemis.native_app.android.service.impl.courses

import de.tum.informatics.www1.artemis.native_app.android.content.Course
import de.tum.informatics.www1.artemis.native_app.android.content.account.Account
import de.tum.informatics.www1.artemis.native_app.android.service.CourseRegistrationService
import de.tum.informatics.www1.artemis.native_app.android.service.impl.KtorProvider
import de.tum.informatics.www1.artemis.native_app.android.util.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.android.util.performNetworkCall
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class CourseRegistrationServiceImpl(
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
                        appendPathSegments("api", "courses", "for-registration")
                    }

                    bearerAuth(authToken)
                    contentType(ContentType.Application.Json)
                }
                .body()
        }
    }

    override suspend fun registerInCourse(
        serverUrl: String,
        authToken: String,
        courseId: Int
    ): NetworkResponse<Unit> {
        return performNetworkCall {
            val user: Account = ktorProvider.ktorClient
                .post(serverUrl) {
                    url {
                        appendPathSegments("api", courseId.toString(), "register")
                    }

                    bearerAuth(authToken)
                }.body()

            //TODO: sync groups
        }
    }
}