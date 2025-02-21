package de.tum.informatics.www1.artemis.native_app.feature.force_update.repository

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

}
