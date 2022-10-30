package de.tum.informatics.www1.artemis.native_app.android.service.student

import de.tum.informatics.www1.artemis.native_app.android.content.Course
import de.tum.informatics.www1.artemis.native_app.android.util.NetworkResponse

/**
 * Service that handles all server communication for registering to a course.
 */
interface CourseRegistrationService {

    /**
     * Fetch the courses the user can register to from the server.
     */
    suspend fun fetchRegistrableCourses(serverUrl: String, authToken: String): NetworkResponse<List<Course>>

    suspend fun registerInCourse(serverUrl: String, authToken: String, courseId: Int): NetworkResponse<Unit>
}