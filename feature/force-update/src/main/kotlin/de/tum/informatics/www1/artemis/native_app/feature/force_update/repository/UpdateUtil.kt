package de.tum.informatics.www1.artemis.native_app.feature.force_update.repository

import de.tum.informatics.www1.artemis.native_app.core.common.app_version.NormalizedAppVersion
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import java.util.concurrent.TimeUnit

object UpdateUtil {

    /**
     * Determines if it's time to check for an update (every 2 days).
     */
    fun isTimeToCheckUpdate(lastCheckTime: Long, now: Long): Boolean {
        return (now - lastCheckTime) >= TimeUnit.DAYS.toMillis(2)
    }

    /**
     * Creates an UpdateResult based on the network response.
     *
     * If the response is successful, it normalizes the server version (or uses "0.0.0" if missing),
     * then compares it with the current version. If an update is required, it calls [onUpdateDetected]
     * with the new version so that the caller can persist it.
     *
     * In case of a failure response, it returns an UpdateResult indicating no update.
     */
    suspend fun createUpdateResultBasedOnServiceResponse(
        response: NetworkResponse<NormalizedAppVersion>,
        currentVersion: NormalizedAppVersion,
        storedServerVersion: NormalizedAppVersion,
        onUpdateDetected: suspend (newVersion: NormalizedAppVersion) -> Unit
    ): UpdateRepository.UpdateResult {
        return when (response) {
            is NetworkResponse.Response -> {
                val serverMinVersion = response.data
                val updateRequired = serverMinVersion > currentVersion
                if (updateRequired) {
                    onUpdateDetected(serverMinVersion)
                }
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
                minVersion = storedServerVersion
            )
        }
    }
}
