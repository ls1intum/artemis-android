package de.tum.informatics.www1.artemis.native_app.core.data.service

import de.tum.informatics.www1.artemis.native_app.android.model.Course
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import kotlinx.coroutines.flow.Flow

interface CourseService {

    suspend fun getCourse(courseId: Int, serverUrl: String, authToken: String): Flow<DataState<Course>>
}