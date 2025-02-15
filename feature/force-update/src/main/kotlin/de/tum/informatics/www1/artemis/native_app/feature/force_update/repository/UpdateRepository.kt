package de.tum.informatics.www1.artemis.native_app.feature.force_update.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.feature.force_update.service.UpdateService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

private val Context.dataStore by preferencesDataStore("update_preferences")

class UpdateRepository(
    private val context: Context,
    private val updateService: UpdateService,
    private val versionCode: Int
) {

    companion object {
        private val LAST_UPDATE_CHECK = longPreferencesKey("last_update_check")
        private val LAST_KNOWN_VERSION = intPreferencesKey("last_known_version")
    }

    suspend fun checkForUpdate(): UpdateCheckResult {
        val storedServerVersion = context.dataStore.data.map { it[LAST_KNOWN_VERSION] ?: 0 }.first()

        if (storedServerVersion > versionCode) {
            return UpdateCheckResult(updateRequired = true, forceUpdate = storedServerVersion > versionCode + 1)
        }

        if (!isTimeToCheckUpdate()) {
            return UpdateCheckResult(false, false)
        }

        return when (val response = updateService.getLatestVersion()) {
            is NetworkResponse.Response -> {
                val latestVersion = response.data
                val forceUpdate = (latestVersion > versionCode + 1)

                // If there's an update, record new version & timestamp
                if (latestVersion > versionCode) {
                    saveLastUpdateCheck(System.currentTimeMillis())
                    saveLastKnownVersion(latestVersion)
                }
                UpdateCheckResult(latestVersion > versionCode, forceUpdate)
            }
            else -> UpdateCheckResult(false, false) // Silent fail
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

    data class UpdateCheckResult(val updateRequired: Boolean, val forceUpdate: Boolean)
}
