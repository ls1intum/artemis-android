package de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context

import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider

interface CourseBasedService: ArtemisContextBasedService<ArtemisContext.Course>

abstract class CourseBasedServiceImpl(
    ktorProvider: KtorProvider,
    artemisContextProvider: ArtemisContextProvider,
) : ArtemisContextBasedServiceImpl<ArtemisContext.Course>(
    ktorProvider,
    artemisContextProvider,
    ArtemisContext.Course::class
) {
    suspend fun courseId(): Long = artemisContext().courseId
}