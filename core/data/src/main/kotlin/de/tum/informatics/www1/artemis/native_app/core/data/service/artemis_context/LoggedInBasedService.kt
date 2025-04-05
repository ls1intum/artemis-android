package de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context

import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

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

    override val onArtemisContextChanged: Flow<ArtemisContext.LoggedIn> = filteredArtemisContextFlow
        .distinctUntilChanged { old, new ->
        // Consider contexts equal if they have the same data, regardless of type
        // This prevents emissions when only the context type changes
        old.serverUrl == new.serverUrl && old.authToken == new.authToken
    }
}