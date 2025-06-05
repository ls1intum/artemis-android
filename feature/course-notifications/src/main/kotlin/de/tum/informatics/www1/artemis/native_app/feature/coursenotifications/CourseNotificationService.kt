package de.tum.informatics.www1.artemis.native_app.feature.coursenotifications

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.LoggedInBasedService

/*
* Service where you can make requests about the course-wide notifications.
*/
interface CourseNotificationService : LoggedInBasedService {

    suspend fun loadCourseNotifications(
        courseId: Long,
        page: Int = 0,
        size: Int = 50
    ): NetworkResponse<CourseNotification>
}