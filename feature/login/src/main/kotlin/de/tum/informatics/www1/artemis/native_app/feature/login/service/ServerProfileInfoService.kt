package de.tum.informatics.www1.artemis.native_app.feature.login.service

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.model.server_config.ProfileInfo

interface ServerProfileInfoService {

    /**
     * Load the server profile info for the given server url.
     */
    suspend fun getServerProfileInfo(serverUrl: String): NetworkResponse<ProfileInfo>
}