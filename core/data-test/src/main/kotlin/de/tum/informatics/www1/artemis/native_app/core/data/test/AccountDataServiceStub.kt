package de.tum.informatics.www1.artemis.native_app.core.data.test

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.AccountDataService
import de.tum.informatics.www1.artemis.native_app.core.model.account.Account
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class AccountDataServiceStub(private val account: Account = Account()) : AccountDataService {
    override val onReloadRequired: Flow<Unit> = emptyFlow()

    override suspend fun getAccountData(): NetworkResponse<Account> = NetworkResponse.Response(account)

    override suspend fun getCachedAccountData(): Account = account
}
