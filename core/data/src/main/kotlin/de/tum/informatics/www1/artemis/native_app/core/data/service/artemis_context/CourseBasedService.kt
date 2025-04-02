package de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context

import de.tum.informatics.www1.artemis.native_app.core.data.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.data.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

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

    override val onArtemisContextChanged: Flow<ArtemisContext.Course> = filteredArtemisContextFlow
        .distinctUntilChanged { old, new ->
            // Consider contexts equal if they have the same data, regardless of type
            // This prevents emissions when only the context type changes
            old.serverUrl == new.serverUrl
                    && old.authToken == new.authToken
                    && old.account == new.account
                    && old.courseId == new.courseId
        }
}