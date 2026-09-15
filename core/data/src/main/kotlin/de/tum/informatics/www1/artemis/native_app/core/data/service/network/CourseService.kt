package de.tum.informatics.www1.artemis.native_app.core.data.service.network

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.LoggedInBasedService
import de.tum.informatics.www1.artemis.native_app.core.model.Course

interface CourseService : LoggedInBasedService {

    /**
     * The course itself, without its exercises and lectures.
     *
     * Use this wherever only course metadata is needed, for example to read the communication
     * configuration or the code of conduct.
     */
    suspend fun getCourse(courseId: Long): NetworkResponse<Course>

    /**
     * The course together with its exercises and lectures, and with [Course.faqEnabled] filled in
     * from the course's available tabs.
     *
     * Artemis 10 split the former single-course dashboard endpoint into one endpoint per kind of
     * content, so this issues those requests in parallel and merges the responses.
     */
    suspend fun getCourseWithContent(courseId: Long): NetworkResponse<Course>
}
