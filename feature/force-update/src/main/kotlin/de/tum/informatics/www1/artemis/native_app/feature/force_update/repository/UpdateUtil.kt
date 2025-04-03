package de.tum.informatics.www1.artemis.native_app.feature.force_update.repository

import android.util.Log
import de.tum.informatics.www1.artemis.native_app.core.common.app_version.NormalizedAppVersion
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.feature.force_update.FeatureAvailability
import de.tum.informatics.www1.artemis.native_app.feature.force_update.service.UpdateServiceResult

private const val CHECK_INTERVAL_MS = 60_000L // 60 seconds

object UpdateUtil {

    fun isTimeToCheckUpdate(lastCheckTime: Long, now: Long): Boolean {
        return (now - lastCheckTime) >= CHECK_INTERVAL_MS
    }

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

                Log.d("TAG", "Current version: $currentVersion")
                Log.d("TAG", "Min required version: ${data.minVersion}")
                Log.d("TAG", "Recommended version: ${data.recommendedVersion}")
                Log.d("TAG", "Available features: ${data.features.joinToString()}")

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