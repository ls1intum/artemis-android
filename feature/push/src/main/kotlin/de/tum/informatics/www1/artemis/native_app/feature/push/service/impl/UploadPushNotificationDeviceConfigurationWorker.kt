package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.messaging.FirebaseMessaging
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationConfigurationService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

internal class UploadPushNotificationDeviceConfigurationWorker(
    appContext: Context,
    params: WorkerParameters,
    private val pushNotificationConfigurationService: PushNotificationConfigurationService,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService
) : CoroutineWorker(appContext, params) {

    companion object {
        private const val TAG = "UploadPushNotificationDeviceConfigurationWorker"
    }

    override suspend fun doWork(): Result {
        val token = try {
            FirebaseMessaging
                .getInstance()
                .token
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Could not get firebase token", e)
            return Result.failure()
        }

        val aesKey = pushNotificationConfigurationService.getOrCreateCurrentAESKey()

        val authToken = when (val authData = accountService.authenticationData.first()) {
            is AccountService.AuthenticationData.LoggedIn -> authData.authToken
            AccountService.AuthenticationData.NotLoggedIn -> {
                Log.e(TAG, "User is not logged in. Cannot upload token.")
                return Result.failure()
            }
        }

        return pushNotificationConfigurationService
            .uploadPushNotificationDeviceConfigurationsToServer(
                serverUrl = serverConfigurationService.serverUrl.first(),
                authToken = authToken,
                aesKey = aesKey,
                firebaseToken = token
            )
            .toWorkerResult()
    }
}