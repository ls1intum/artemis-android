package de.tum.informatics.www1.artemis.native_app.android.service

import de.tum.informatics.www1.artemis.native_app.android.content.Course

/**
 * Service that handles all server communication for registering to a course.
 */
interface CourseRegistrationService {

    /**
     * Fetch the courses the user can register to from the server.
     */
    suspend fun fetchRegistrableCourses(): List<Course>
}