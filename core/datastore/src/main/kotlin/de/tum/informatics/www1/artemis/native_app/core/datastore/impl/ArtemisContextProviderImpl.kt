package de.tum.informatics.www1.artemis.native_app.core.datastore.impl

import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ArtemisContextProviderImpl(
    serverConfigurationService: ServerConfigurationService,
    accountService: AccountService
) : ArtemisContextProvider {

    override val current: Flow<ArtemisContext> = combine(
        serverConfigurationService.serverUrl,
        accountService.authToken
    ) { serverUrl, authToken ->
        ArtemisContext(serverUrl, authToken)
    }
}