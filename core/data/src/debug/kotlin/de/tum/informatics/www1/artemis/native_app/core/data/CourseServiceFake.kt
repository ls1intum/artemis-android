package de.tum.informatics.www1.artemis.native_app.core.data

import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.CourseService
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import kotlinx.coroutines.flow.emptyFlow

class CourseServiceFake(private val course: Course) : CourseService {

    override val onArtemisContextChanged = emptyFlow<ArtemisContext.LoggedIn>()

    override suspend fun getCourse(
        courseId: Long,
    ): NetworkResponse<Course> = NetworkResponse.Response(course)

    override suspend fun getCourseWithContent(
        courseId: Long,
    ): NetworkResponse<Course> = NetworkResponse.Response(course)
}
