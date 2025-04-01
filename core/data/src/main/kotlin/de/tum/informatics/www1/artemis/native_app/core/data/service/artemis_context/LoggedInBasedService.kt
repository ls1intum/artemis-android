package de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context

import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider

interface LoggedInBasedService: ArtemisContextBasedService<ArtemisContext.LoggedIn>

abstract class LoggedInBasedServiceImpl(
    ktorProvider: KtorProvider,
    artemisContextProvider: ArtemisContextProvider,
) : ArtemisContextBasedServiceImpl<ArtemisContext.LoggedIn>(
    ktorProvider,
    artemisContextProvider,
    ArtemisContext.LoggedIn::class
) {
    suspend fun authToken(): String = artemisContext().authToken
}