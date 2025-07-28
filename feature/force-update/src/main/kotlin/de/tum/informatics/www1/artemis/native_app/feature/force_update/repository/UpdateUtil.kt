package de.tum.informatics.www1.artemis.native_app.feature.force_update.repository

import de.tum.informatics.www1.artemis.native_app.core.common.FeatureAvailability
import de.tum.informatics.www1.artemis.native_app.core.common.app_version.NormalizedAppVersion
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.feature.force_update.service.UpdateServiceResult

object UpdateUtil {
    /**
     * Creates an UpdateResult based on the network response.
     *
     * If the response is successful, it normalizes the server version (or uses "0.0.0" if missing),
     * then compares it with the current version. Along with the feature toggles
     *
     * In case of a failure response, it returns an UpdateResult indicating no update.
     */
    fun processUpdateResponse(
        response: NetworkResponse<UpdateServiceResult>,
        currentVersion: NormalizedAppVersion
    ): UpdateRepository.UpdateResult {
        return when (response) {
            is NetworkResponse.Response -> {
                val data = response.data

                // 🔁 Set feature list
                FeatureAvailability.setAvailableFeatures(data.features)
                FeatureAvailability.setActiveModuleFeatures(data.activeModuleFeatures)

                // 🔍 Version check
                val serverMinVersion = data.minVersion
                val serverRecommendedVersion = data.recommendedVersion
                
                // Check if update is required (current version below min version)
                val updateRequired = serverMinVersion > currentVersion
                
                // Check if update is recommended (current version below recommended version but above min version)
                val updateRecommended = !updateRequired && serverRecommendedVersion > currentVersion

                UpdateRepository.UpdateResult(
                    updateAvailable = updateRequired || updateRecommended,
                    forceUpdate = updateRequired,
                    currentVersion = currentVersion,
                    minVersion = serverMinVersion,
                    recommendedVersion = serverRecommendedVersion,
                    showRecommended = updateRecommended
                )
            }

            else -> UpdateRepository.UpdateResult(
                updateAvailable = false,
                forceUpdate = false,
                currentVersion = currentVersion,
                minVersion = currentVersion,
                recommendedVersion = currentVersion,
                showRecommended = false
            )
        }
    }
}