package de.tum.informatics.www1.artemis.native_app.core.data.service.network

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.model.account.Account

interface AccountDataService {

    suspend fun getAccountData(serverUrl: String, bearerToken: String): NetworkResponse<Account>

    suspend fun getCachedAccountData(serverUrl: String, bearerToken: String): Account?
}
