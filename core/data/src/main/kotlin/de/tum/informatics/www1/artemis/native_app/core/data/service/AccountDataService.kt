package de.tum.informatics.www1.artemis.native_app.core.data.service

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.model.account.Account
import de.tum.informatics.www1.artemis.native_app.core.model.server_config.ProfileInfo

interface AccountDataService {

    suspend fun getAccountData(serverUrl: String, bearerToken: String): NetworkResponse<Account>
}