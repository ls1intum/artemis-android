package de.tum.informatics.www1.artemis.native_app.feature.force_update.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.feature.force_update.service.UpdateService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import java.util.concurrent.TimeUnit

private val Context.dataStore by preferencesDataStore("update_preferences")

class UpdateRepository(
    private val context: Context,
    private val updateService: UpdateService,
    version: String,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
) {

    companion object {
        private val LAST_UPDATE_CHECK = longPreferencesKey("last_update_check")
        private val LAST_KNOWN_VERSION = stringPreferencesKey("last_known_version")
    }

    /**
     * Normalize version E.g., "1.x.x-prod" becomes "1.x.x".
     */
    private fun normalizeVersion(version: String): String =
        version.substringBefore("-").trim()

    /**
     * Returns true if the serverVersion (normalized) is greater than currentVersion.
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

    private val currentVersionNormalized = normalizeVersion(version)

    /**
     * Checks for an update whenever the server URL changes or every 2 days.
     * Instead of using an integer version code, we compare version strings.
     */
    fun checkForUpdate(): Flow<UpdateResult> =
        serverConfigurationService.serverUrl.mapLatest { latestServerUrl ->

            // Read the stored server version from DataStore; default to "0.0.0" if none.
            val storedServerVersion = context.dataStore.data
                .map { it[LAST_KNOWN_VERSION] ?: "0.0.0" }
                .first()

            // If the stored version is greater than our current version, then an update is required.
            if (isVersionGreater(storedServerVersion, currentVersionNormalized)) {
                return@mapLatest UpdateResult(
                    updateAvailable = true,
                    forceUpdate = true,
                    currentVersion = currentVersionNormalized,
                    minVersion = storedServerVersion
                )
            }

            // If it's not yet time to re-check (less than 2 days since last check), then assume no update.
            if (!isTimeToCheckUpdate()) {
                return@mapLatest UpdateResult(
                    updateAvailable = false,
                    forceUpdate = false,
                    currentVersion = currentVersionNormalized,
                    minVersion = storedServerVersion
                )
            }

            val token = accountService.authToken.first()
            val response = updateService.getLatestVersion(latestServerUrl, token)

            when (response) {
                is NetworkResponse.Response -> {
                    // Use the server response—if it's null or blank, treat it as "0.0.0".
                    val serverMinVersion = normalizeVersion(response.data ?: "0.0.0")

                    val updateRequired =
                        isVersionGreater(serverMinVersion, currentVersionNormalized)
                    if (updateRequired) {
                        saveLastKnownVersion(serverMinVersion)
                        saveLastUpdateCheck(System.currentTimeMillis())
                    }

                    UpdateResult(
                        updateAvailable = updateRequired,
                        forceUpdate = updateRequired,
                        currentVersion = currentVersionNormalized,
                        minVersion = serverMinVersion
                    )
                }
                // If the network call fails, assume no update.
                else -> UpdateResult(
                    updateAvailable = false,
                    forceUpdate = false,
                    currentVersion = currentVersionNormalized,
                    minVersion = storedServerVersion
                )
            }
        }

    private suspend fun isTimeToCheckUpdate(): Boolean {
        val lastCheckTime = context.dataStore.data
            .map { it[LAST_UPDATE_CHECK] ?: 0L }
            .first()
        val now = System.currentTimeMillis()
        return (now - lastCheckTime) >= TimeUnit.DAYS.toMillis(2)
    }

    private suspend fun saveLastUpdateCheck(timestamp: Long) {
        context.dataStore.edit { it[LAST_UPDATE_CHECK] = timestamp }
    }

    private suspend fun saveLastKnownVersion(version: String) {
        context.dataStore.edit { it[LAST_KNOWN_VERSION] = version }
    }

    data class UpdateResult(
        val updateAvailable: Boolean,
        val forceUpdate: Boolean,
        val currentVersion: String,
        val minVersion: String
    )
}
