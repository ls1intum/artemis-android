package de.tum.informatics.www1.artemis.native_app.core.data.service.network.impl

import de.tum.informatics.www1.artemis.native_app.core.data.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.Api
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.LoggedInBasedServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.CourseService
import de.tum.informatics.www1.artemis.native_app.core.model.CourseWithScore
import io.ktor.http.appendPathSegments

internal class CourseServiceImpl(
    ktorProvider: KtorProvider,
    artemisContextProvider: ArtemisContextProvider,
) : LoggedInBasedServiceImpl(ktorProvider,artemisContextProvider), CourseService {

    override suspend fun getCourse(
        courseId: Long,
    ): NetworkResponse<CourseWithScore> {
        return getRequest {
            url {
                appendPathSegments(*Api.Core.Courses.path, courseId.toString(), "for-dashboard")
            }
        }
    }
}