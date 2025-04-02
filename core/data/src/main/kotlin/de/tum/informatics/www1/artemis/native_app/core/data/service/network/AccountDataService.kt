package de.tum.informatics.www1.artemis.native_app.core.data.service.network

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.LoggedInBasedService
import de.tum.informatics.www1.artemis.native_app.core.model.account.Account
import kotlinx.coroutines.flow.Flow

interface AccountDataService : LoggedInBasedService {

    val accountDataFlow: Flow<Account?>

    suspend fun getAccountData(): NetworkResponse<Account>

    suspend fun getCachedAccountData(): Account?
}
