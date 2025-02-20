package de.tum.informatics.www1.artemis.native_app.feature.force_update.service

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerProfileInfoService

interface UpdateService {
    suspend fun getLatestVersion(
        serverUrl: String,
        authToken: String
    ): NetworkResponse<String?>
}

class UpdateServiceImpl(
    private val serverProfileInfoService: ServerProfileInfoService
) : UpdateService {

    override suspend fun getLatestVersion(
        serverUrl: String,
        authToken: String
    ): NetworkResponse<String?> {
        return when (val response = serverProfileInfoService.getServerProfileInfo(serverUrl)) {
            is NetworkResponse.Response -> NetworkResponse.Response(response.data.compatibleVersions?.android?.minRequired)
            is NetworkResponse.Failure -> NetworkResponse.Response(null)
        }
    }
}






