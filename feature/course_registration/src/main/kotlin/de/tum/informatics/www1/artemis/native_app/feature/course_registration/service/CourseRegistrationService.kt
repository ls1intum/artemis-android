package de.tum.informatics.www1.artemis.native_app.feature.course_registration.service

import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.Flow

/**
 * Service that handles all server communication for registering to a course.
 */
interface CourseRegistrationService {

    /**
     * Fetch the courses the user can register to from the server.
     */
    suspend fun fetchRegistrableCourses(serverUrl: String, authToken: String): NetworkResponse<List<Course>>

    suspend fun registerInCourse(serverUrl: String, authToken: String, courseId: Long): NetworkResponse<HttpStatusCode>
}