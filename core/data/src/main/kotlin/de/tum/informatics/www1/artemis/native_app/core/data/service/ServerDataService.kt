package de.tum.informatics.www1.artemis.native_app.core.data.service

import de.tum.informatics.www1.artemis.native_app.core.model.account.Account
import de.tum.informatics.www1.artemis.native_app.core.model.server_config.ProfileInfo
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import kotlinx.coroutines.flow.Flow

interface ServerDataService {

    /**
     * Load the server profile info for the given server url.
     */
    fun getServerProfileInfo(serverUrl: String): Flow<DataState<ProfileInfo>>

    fun getAccountData(serverUrl: String, bearerToken: String): Flow<DataState<Account>>
}