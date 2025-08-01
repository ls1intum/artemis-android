package de.tum.informatics.www1.artemis.native_app.feature.force_update.repository

import de.tum.informatics.www1.artemis.native_app.core.common.app_version.AppVersionProvider
import de.tum.informatics.www1.artemis.native_app.core.common.app_version.NormalizedAppVersion
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.feature.force_update.service.UpdateService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first

class UpdateRepository(
    private val updateService: UpdateService,
    appVersionProvider: AppVersionProvider,
    private val serverConfigurationService: ServerConfigurationService
) {

    private val _updateResultFlow = MutableStateFlow<UpdateResult?>(null)
    val updateResultFlow: StateFlow<UpdateResult?> = _updateResultFlow

    private var lastCheckedTimestamp: Long = 0L
    private var lastResult: UpdateResult? = null
    private var isCurrentRecommendedUpdateDismissed = false

    private val currentVersionNormalized = appVersionProvider.appVersion.normalized

    suspend fun triggerUpdateCheck() {
        val now = System.currentTimeMillis()

        val serverUrl = serverConfigurationService.serverUrl.first()
        val response = updateService.getProfileInfo(serverUrl)
        val result = UpdateUtil.processUpdateResponse(
            response = response,
            currentVersion = currentVersionNormalized
        )
        
        // Check if this recommended update should be shown
        val shouldShowRecommended = if (result.updateAvailable && !result.forceUpdate) {
            !isCurrentRecommendedUpdateDismissed
        } else {
            result.updateAvailable
        }
        
        val finalResult = result.copy(updateAvailable = shouldShowRecommended)
        
        lastCheckedTimestamp = now
        lastResult = finalResult
        _updateResultFlow.value = finalResult
    }

    fun dismissRecommendedUpdate() {
        isCurrentRecommendedUpdateDismissed = true
        // Update the current result to not show the update
        lastResult?.let { result ->
            if (!result.forceUpdate) {
                _updateResultFlow.value = result.copy(updateAvailable = false)
            }
        }
    }

    data class UpdateResult(
        val updateAvailable: Boolean,
        val forceUpdate: Boolean,
        val currentVersion: NormalizedAppVersion,
        val minVersion: NormalizedAppVersion,
        val recommendedVersion: NormalizedAppVersion
    )
}
