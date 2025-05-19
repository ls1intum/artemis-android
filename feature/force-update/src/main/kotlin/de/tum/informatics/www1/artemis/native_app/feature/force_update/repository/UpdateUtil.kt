package de.tum.informatics.www1.artemis.native_app.feature.force_update.repository

import de.tum.informatics.www1.artemis.native_app.core.common.app_version.NormalizedAppVersion
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.feature.force_update.FeatureAvailability
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

                // 🔍 Version check
                val serverMinVersion = data.minVersion
                val updateRequired = serverMinVersion > currentVersion

                UpdateRepository.UpdateResult(
                    updateAvailable = updateRequired,
                    forceUpdate = updateRequired,
                    currentVersion = currentVersion,
                    minVersion = serverMinVersion
                )
            }

            else -> UpdateRepository.UpdateResult(
                updateAvailable = false,
                forceUpdate = false,
                currentVersion = currentVersion,
                minVersion = currentVersion
            )
        }
    }
}