package de.tum.informatics.www1.artemis.native_app.feature.force_update.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.tum.informatics.www1.artemis.native_app.core.common.app_version.AppVersionProvider
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.feature.force_update.service.UpdateService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest

private val Context.dataStore by preferencesDataStore("update_preferences")

class UpdateRepository(
    private val context: Context,
    private val updateService: UpdateService,
    appVersionProvider: AppVersionProvider,
    serverConfigurationService: ServerConfigurationService,
) {

    companion object {
        private val LAST_UPDATE_CHECK = longPreferencesKey("last_update_check")
        private val LAST_KNOWN_VERSION = stringPreferencesKey("last_known_version")
    }

    private val currentVersionNormalized = appVersionProvider.appVersion.normalized

    /**
     * Checks for an update whenever the server URL changes or every 2 days.
     * Instead of using an integer version code, we compare version strings.
     */
    val updateResultFlow: Flow<UpdateResult> =
        serverConfigurationService.serverUrl.mapLatest { latestServerUrl ->

            // Read the stored server version from DataStore; default to "0.0.0" if none.
            val storedServerVersion = context.dataStore.data
                .map { it[LAST_KNOWN_VERSION] ?: "0.0.0" }
                .first()

            val noUpdateNeeded =  UpdateResult(
                updateAvailable = false,
                forceUpdate = false,
                currentVersion = currentVersionNormalized,
                minVersion = storedServerVersion
            )

            // If the stored version is greater than our current version, then an update is required.
            if (UpdateUtil.isVersionGreater(storedServerVersion, currentVersionNormalized)) {
                return@mapLatest UpdateResult(
                    updateAvailable = true,
                    forceUpdate = true,
                    currentVersion = currentVersionNormalized,
                    minVersion = storedServerVersion
                )
            }

            val lastCheckTime = context.dataStore.data
                .map { it[LAST_UPDATE_CHECK] ?: 0L }
                .first()

            // If it's not yet time to re-check (less than 2 days since last check), then assume no update.
            if (!UpdateUtil.isTimeToCheckUpdate(lastCheckTime, System.currentTimeMillis())) {
                return@mapLatest noUpdateNeeded
            }

            val response = updateService.getLatestVersion(latestServerUrl)

            UpdateUtil.createUpdateResultBasedOnServiceResponse(
                response = response,
                currentVersion = currentVersionNormalized,
                storedServerVersion = storedServerVersion,
                onUpdateDetected = { newVersion ->
                    saveLastKnownVersion(newVersion)
                    saveLastUpdateCheck(System.currentTimeMillis())
                }
            )
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
