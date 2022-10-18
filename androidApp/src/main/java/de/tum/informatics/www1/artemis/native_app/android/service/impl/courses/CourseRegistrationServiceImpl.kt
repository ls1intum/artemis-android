package de.tum.informatics.www1.artemis.native_app.android.service.impl.courses

import de.tum.informatics.www1.artemis.native_app.android.content.Course
import de.tum.informatics.www1.artemis.native_app.android.service.CourseRegistrationService
import de.tum.informatics.www1.artemis.native_app.android.service.ServerCommunicationProvider
import de.tum.informatics.www1.artemis.native_app.android.service.impl.KtorProvider
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class CourseRegistrationServiceImpl(
    private val ktorProvider: KtorProvider
) : CourseRegistrationService {

    override suspend fun fetchRegistrableCourses(serverUrl: String, authToken: String): List<Course> {
        return ktorProvider.ktorClient
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