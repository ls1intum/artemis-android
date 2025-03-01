package de.tum.informatics.www1.artemis.native_app.feature.courseregistration.service.impl

import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.ApiEndpoint
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.ArtemisContextBasedServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.feature.courseregistration.service.CourseRegistrationService
import io.ktor.http.HttpStatusCode
import io.ktor.http.appendPathSegments

internal class CourseRegistrationServiceImpl(
    ktorProvider: KtorProvider,
    artemisContextProvider: ArtemisContextProvider,
) : ArtemisContextBasedServiceImpl(ktorProvider, artemisContextProvider), CourseRegistrationService {

    override suspend fun fetchRegistrableCourses(): NetworkResponse<List<Course>> {
        return getRequest {
            url {
                appendPathSegments(*ApiEndpoint.core_courses, "for-enrollment")
            }
        }
    }

    override suspend fun registerInCourse(courseId: Long): NetworkResponse<HttpStatusCode> {
        return postRequest {
            url {
                appendPathSegments(*ApiEndpoint.core_courses, courseId.toString(), "enroll")
            }
        }
    }
}