package de.tum.informatics.www1.artemis.native_app.feature.force_update.service

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerProfileInfoService

interface UpdateService {
    suspend fun getLatestVersion(
        serverUrl: String,
    ): NetworkResponse<String?>
}

class UpdateServiceImpl(
    private val serverProfileInfoService: ServerProfileInfoService
) : UpdateService {

    override suspend fun getLatestVersion(
        serverUrl: String,
    ): NetworkResponse<String?> {
        return serverProfileInfoService.getServerProfileInfo(serverUrl)
            .bind { profileInfo ->
               profileInfo.compatibleVersions?.android?.minRequired
            }
    }
}






