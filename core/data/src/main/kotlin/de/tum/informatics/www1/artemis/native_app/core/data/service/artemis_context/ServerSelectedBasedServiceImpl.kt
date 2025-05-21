package de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context

import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

open class ServerSelectedBasedServiceImpl(
    ktorProvider: KtorProvider,
    artemisContextProvider: ArtemisContextProvider,
) : ArtemisContextBasedServiceImpl<ArtemisContext>(
    ktorProvider,
    artemisContextProvider,
    ArtemisContext::class
) {

    override val onArtemisContextChanged: Flow<ArtemisContext> = filteredArtemisContextFlow
        .distinctUntilChanged { old, new ->
            // Consider contexts equal if they have the same data, regardless of type
            // This prevents emissions when only the context type changes
            old.serverUrl == new.serverUrl
        }

}