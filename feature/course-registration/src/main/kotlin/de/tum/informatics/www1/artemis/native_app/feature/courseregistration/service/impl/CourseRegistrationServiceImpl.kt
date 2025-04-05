package de.tum.informatics.www1.artemis.native_app.feature.courseregistration.service.impl

import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.Api
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.LoggedInBasedServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.feature.courseregistration.service.CourseRegistrationService
import io.ktor.http.HttpStatusCode
import io.ktor.http.appendPathSegments

internal class CourseRegistrationServiceImpl(
    ktorProvider: KtorProvider,
    artemisContextProvider: ArtemisContextProvider,
) : LoggedInBasedServiceImpl(ktorProvider, artemisContextProvider), CourseRegistrationService {

    override suspend fun fetchRegistrableCourses(): NetworkResponse<List<Course>> {
        return getRequest {
            url {
                appendPathSegments(*Api.Core.Courses.path, "for-enrollment")
            }
        }
    }

    override suspend fun registerInCourse(courseId: Long): NetworkResponse<HttpStatusCode> {
        return postRequest {
            url {
                appendPathSegments(*Api.Core.Courses.path, courseId.toString(), "enroll")
            }
        }
    }
}