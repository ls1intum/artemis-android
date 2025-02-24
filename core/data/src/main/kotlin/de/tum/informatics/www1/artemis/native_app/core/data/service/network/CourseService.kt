package de.tum.informatics.www1.artemis.native_app.core.data.service.network

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.ArtemisContextBasedService
import de.tum.informatics.www1.artemis.native_app.core.model.CourseWithScore

interface CourseService : ArtemisContextBasedService {

    suspend fun getCourse(courseId: Long): NetworkResponse<CourseWithScore>
}
