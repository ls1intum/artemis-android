package de.tum.informatics.www1.artemis.native_app.core.data.service

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.model.CourseWithScore

interface CourseService {

    suspend fun getCourse(courseId: Long, serverUrl: String, authToken: String): NetworkResponse<CourseWithScore>
}
