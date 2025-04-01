package de.tum.informatics.www1.artemis.native_app.feature.courseregistration.service

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.LoggedInBasedService
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import io.ktor.http.HttpStatusCode

/**
 * Service that handles all server communication for registering to a course.
 */
interface CourseRegistrationService: LoggedInBasedService {

    /**
     * Fetch the courses the user can register to from the server.
     */
    suspend fun fetchRegistrableCourses(): NetworkResponse<List<Course>>

    suspend fun registerInCourse(courseId: Long): NetworkResponse<HttpStatusCode>
}