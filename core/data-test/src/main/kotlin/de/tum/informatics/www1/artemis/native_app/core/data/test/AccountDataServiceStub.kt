package de.tum.informatics.www1.artemis.native_app.core.data.test

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.AccountDataService
import de.tum.informatics.www1.artemis.native_app.core.model.account.Account

class AccountDataServiceStub(private val account: Account = Account()) : AccountDataService {
    override suspend fun getAccountData(): NetworkResponse<Account> = NetworkResponse.Response(account)

    override suspend fun getCachedAccountData(): Account = account
}
