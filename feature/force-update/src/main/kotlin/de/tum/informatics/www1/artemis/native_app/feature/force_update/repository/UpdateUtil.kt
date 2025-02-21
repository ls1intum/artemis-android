package de.tum.informatics.www1.artemis.native_app.feature.force_update.repository

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import java.util.concurrent.TimeUnit

object UpdateUtil {

    /**
     * Normalize version E.g., "1.x.x-prod" becomes "1.x.x".
     */
    fun normalizeVersion(version: String): String =
        version.substringBefore("-").trim()

    /**
     * Compare two versions and return true if `serverVersion` is greater than `currentVersion`.
     */
    fun isVersionGreater(serverVersion: String, currentVersion: String): Boolean {
        val parts1 = serverVersion.split(".")
        val parts2 = currentVersion.split(".")
        val maxLen = maxOf(parts1.size, parts2.size)

        for (i in 0 until maxLen) {
            val p1 = parts1.getOrNull(i)?.toIntOrNull() ?: 0
            val p2 = parts2.getOrNull(i)?.toIntOrNull() ?: 0
            if (p1 != p2) return p1 > p2
        }
        return false
    }

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
        response: NetworkResponse<String?>,
        currentVersion: String,
        storedServerVersion: String,
        onUpdateDetected: suspend (newVersion: String) -> Unit
    ): UpdateRepository.UpdateResult {
        return when (response) {
            is NetworkResponse.Response -> {
                val serverMinVersion = normalizeVersion(response.data ?: "0.0.0")
                val updateRequired = isVersionGreater(serverMinVersion, currentVersion)
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
