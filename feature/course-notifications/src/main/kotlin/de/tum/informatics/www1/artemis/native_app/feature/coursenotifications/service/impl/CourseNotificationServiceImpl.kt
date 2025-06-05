package de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.service.impl

import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.Api
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.LoggedInBasedServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.course_notification_model.CourseNotification
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.service.CourseNotificationService
import io.ktor.http.appendPathSegments

/**
 * [CourseNotificationService] implementation that requests the course-wide-notifications from the Artemis server.
 */
internal class CourseNotificationServiceImpl(
    ktorProvider: KtorProvider,
    artemisContextProvider: ArtemisContextProvider,
) : LoggedInBasedServiceImpl(ktorProvider, artemisContextProvider), CourseNotificationService {

    override suspend fun loadCourseNotifications(
        courseId: Long,
        page: Int,
        size: Int
    ): NetworkResponse<CourseNotification> {
        return getRequest<CourseNotification> {
            url {
                appendPathSegments(*Api.Communication.CourseNotifications.path, courseId.toString())
                parameters.append("page", page.toString())
                parameters.append("size", size.toString())
            }
        }
    }
}