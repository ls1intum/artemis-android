package de.tum.informatics.www1.artemis.native_app.core.data.service

import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.CourseWithScore
import kotlinx.coroutines.flow.Flow

interface CourseService {

    suspend fun getCourse(courseId: Long, serverUrl: String, authToken: String): NetworkResponse<CourseWithScore>
}