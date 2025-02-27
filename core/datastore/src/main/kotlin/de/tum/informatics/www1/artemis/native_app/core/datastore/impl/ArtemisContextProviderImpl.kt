package de.tum.informatics.www1.artemis.native_app.core.datastore.impl

import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ArtemisContextProviderImpl(
    serverConfigurationService: ServerConfigurationService,
    accountService: AccountService,
) : ArtemisContextProvider {

    override val flow: Flow<ArtemisContext> = combine(
        serverConfigurationService.serverUrl,
        accountService.authenticationData
    ) { serverUrl, authData ->
        when (authData) {
            AccountService.AuthenticationData.NotLoggedIn -> return@combine ArtemisContext.Empty

            is AccountService.AuthenticationData.LoggedIn -> ArtemisContext(
                serverUrl = serverUrl,
                authToken = authData.authToken,
                username = authData.username
            )
        }

    }
}