package de.tum.informatics.www1.artemis.native_app.datastore.test

import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class AccountServiceStub(
    override val authenticationData: Flow<AccountService.AuthenticationData> =
        flowOf(AccountService.AuthenticationData.LoggedIn("", "Kate Bell"))
) : AccountService {
    override suspend fun storeAccessToken(jwt: String, rememberMe: Boolean) = Unit

    override suspend fun logout() = Unit
}
