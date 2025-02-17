package de.tum.informatics.www1.artemis.native_app.feature.force_update.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
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
    private val versionCode: Int,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
) {

    companion object {
        private val LAST_UPDATE_CHECK = longPreferencesKey("last_update_check")
        private val LAST_KNOWN_VERSION = intPreferencesKey("last_known_version")
    }

    /**
     * Called whenever serverUrl changes. We also re-check every 2 days if the stored
     * version is not already higher than the local version.
     */
    fun checkForUpdate(): Flow<UpdateResult> =
        serverConfigurationService.serverUrl.mapLatest { latestServerUrl ->

            //Read the stored server version
            val storedServerVersion = context.dataStore.data
                .map { it[LAST_KNOWN_VERSION] ?: 0 }
                .first()

            // If the stored version is already higher than the local version, we need an update
            if (storedServerVersion > versionCode) {
                return@mapLatest UpdateResult(updateAvailable = true, forceUpdate = true)
            }

            // If it’s not time to check updates yet, exit
            if (!isTimeToCheckUpdate()) {
                return@mapLatest UpdateResult(updateAvailable = false, forceUpdate = false)
            }

            //Do a new call to /management/info
            val token = accountService.authToken.first()
            val response = updateService.getLatestVersion(latestServerUrl, token)

            when (response) {
                is NetworkResponse.Response -> {
                    // parse min version from “compatible-versions”
                    val minVersion: Int = response.data ?: 0

                    val shouldForceUpdate = minVersion > versionCode
                    if (shouldForceUpdate) {
                        // remember new version & timestamp
                        saveLastKnownVersion(minVersion)
                        saveLastUpdateCheck(System.currentTimeMillis())
                    }

                    UpdateResult(
                        updateAvailable = shouldForceUpdate,
                        forceUpdate = shouldForceUpdate
                    )
                }

                // If network fails
                else -> UpdateResult(false, false)
            }
        }

    private suspend fun isTimeToCheckUpdate(): Boolean {
        val lastCheckTime = context.dataStore.data.map { it[LAST_UPDATE_CHECK] ?: 0L }.first()
        val now = System.currentTimeMillis()
        return (now - lastCheckTime) >= TimeUnit.DAYS.toMillis(2)
    }

    private suspend fun saveLastUpdateCheck(timestamp: Long) {
        context.dataStore.edit { it[LAST_UPDATE_CHECK] = timestamp }
    }

    private suspend fun saveLastKnownVersion(version: Int) {
        context.dataStore.edit { it[LAST_KNOWN_VERSION] = version }
    }

    data class UpdateResult(
        val updateAvailable: Boolean,
        val forceUpdate: Boolean
    )
}
