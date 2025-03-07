package de.tum.informatics.www1.artemis.native_app.feature.force_update.service

import de.tum.informatics.www1.artemis.native_app.core.common.app_version.NormalizedAppVersion
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerProfileInfoService

interface UpdateService {
    suspend fun getLatestVersion(
        serverUrl: String,
    ): NetworkResponse<NormalizedAppVersion>
}

class UpdateServiceImpl(
    private val serverProfileInfoService: ServerProfileInfoService
) : UpdateService {

    override suspend fun getLatestVersion(
        serverUrl: String,
    ): NetworkResponse<NormalizedAppVersion> {
        return serverProfileInfoService.getServerProfileInfo(serverUrl)
            .bind { profileInfo ->
                val versionString = profileInfo.compatibleVersions?.android?.minRequired
                NormalizedAppVersion.fromNullable(versionString)
            }
    }
}






