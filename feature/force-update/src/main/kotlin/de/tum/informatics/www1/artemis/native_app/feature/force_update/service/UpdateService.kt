package de.tum.informatics.www1.artemis.native_app.feature.force_update.service

import de.tum.informatics.www1.artemis.native_app.core.common.app_version.NormalizedAppVersion
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerProfileInfoService

interface UpdateService {
    suspend fun getProfileInfo(
        serverUrl: String,
    ): NetworkResponse<UpdateServiceResult>
}

data class UpdateServiceResult(
    val minVersion: NormalizedAppVersion,
    val recommendedVersion: NormalizedAppVersion,
    val features: List<String>
)

class UpdateServiceImpl(
    private val serverProfileInfoService: ServerProfileInfoService
) : UpdateService {

    override suspend fun getProfileInfo(
        serverUrl: String,
    ): NetworkResponse<UpdateServiceResult> {
        return serverProfileInfoService.getServerProfileInfo(serverUrl)
            .bind { profileInfo ->
                val minVersionString = profileInfo.compatibleVersions?.android?.minRequired
                val recommendedVersionString = profileInfo.compatibleVersions?.android?.recommended
                UpdateServiceResult(
                    minVersion = NormalizedAppVersion.fromNullable(minVersionString),
                    recommendedVersion = NormalizedAppVersion.fromNullable(recommendedVersionString),
                    features = profileInfo.features
                )
            }
    }
}






