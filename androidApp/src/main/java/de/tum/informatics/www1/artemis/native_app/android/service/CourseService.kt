package de.tum.informatics.www1.artemis.native_app.android.service

import de.tum.informatics.www1.artemis.native_app.android.content.Course
import de.tum.informatics.www1.artemis.native_app.android.util.NetworkResponse

interface CourseService {

    suspend fun getCourse(courseId: Int, serverUrl: String, authToken: String): NetworkResponse<Course>
}