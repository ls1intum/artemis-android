package de.tum.informatics.www1.artemis.native_app.core.datastore.impl

import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class ArtemisContextProviderImpl(
    serverConfigurationService: ServerConfigurationService,
    accountService: AccountService,
    scope: CoroutineScope = MainScope()
) : ArtemisContextProvider {

    override val stateFlow: StateFlow<ArtemisContext> = combine(
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
    }.stateIn(scope, SharingStarted.Eagerly, ArtemisContext.Empty)
}