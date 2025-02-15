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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
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

    fun checkForUpdate(): Flow<UpdateResult> = flow {
        val storedServerVersion = context.dataStore.data
            .map { it[LAST_KNOWN_VERSION] ?: 0 }
            .first()

        if (storedServerVersion > versionCode) {
            emit(
                UpdateResult(
                    updateAvailable = true,
                    forceUpdate = true
                )
            )
            return@flow
        }

        if (!isTimeToCheckUpdate()) {
            emit(UpdateResult(false, false))
            return@flow
        }

        val response = updateService.getLatestVersion(
            serverConfigurationService.serverUrl.first(),
            accountService.authToken.first()
        )

        val updateCheckResult = when (response) {
            is NetworkResponse.Response -> {
                val latestVersion = response.data
                val shouldForceUpdate = (latestVersion > versionCode)

                if (shouldForceUpdate) {
                    saveLastUpdateCheck(System.currentTimeMillis())
                    saveLastKnownVersion(latestVersion)
                }

                UpdateResult(
                    updateAvailable = shouldForceUpdate,
                    forceUpdate = shouldForceUpdate
                )
            }

            else -> UpdateResult(false, false) // silent fail
            //else -> UpdateResult(true, true)

        }

        emit(updateCheckResult)
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
