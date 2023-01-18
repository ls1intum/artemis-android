package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.PushNotificationConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationJobService
import kotlinx.coroutines.flow.first

internal class UploadPushNotificationDeviceConfigurationWorker(
    appContext: Context,
    params: WorkerParameters,
    private val pushNotificationJobService: PushNotificationJobService,
    private val pushNotificationConfigurationService: PushNotificationConfigurationService,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val token = pushNotificationJobService.firebaseToken.first()
            ?: return Result.success()

        val aesKey = pushNotificationConfigurationService.getOrCreateCurrentAESKey()

        val authToken = when (val authData = accountService.authenticationData.first()) {
            is AccountService.AuthenticationData.LoggedIn -> authData.authToken
            AccountService.AuthenticationData.NotLoggedIn -> return Result.failure()
        }

        return pushNotificationJobService
            .uploadPushNotificationDeviceConfigurationsToServer(
                serverUrl = serverConfigurationService.serverUrl.first(),
                authToken = authToken,
                aesKey = aesKey,
                firebaseToken = token
            )
            .toWorkerResult()
    }
}