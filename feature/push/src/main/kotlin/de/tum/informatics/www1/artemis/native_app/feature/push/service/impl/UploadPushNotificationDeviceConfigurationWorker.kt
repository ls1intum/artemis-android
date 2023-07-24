package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.messaging.FirebaseMessaging
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationConfigurationService
import de.tum.informatics.www1.artemis.native_app.feature.push.service.network.NotificationSettingsService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

internal class UploadPushNotificationDeviceConfigurationWorker(
    appContext: Context,
    params: WorkerParameters,
    private val notificationConfigurationService: PushNotificationConfigurationService,
    private val notificationSettingsService: NotificationSettingsService,
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

        val authToken = when (val authData = accountService.authenticationData.first()) {
            is AccountService.AuthenticationData.LoggedIn -> authData.authToken
            AccountService.AuthenticationData.NotLoggedIn -> {
                Log.e(TAG, "User is not logged in. Cannot upload token.")
                return Result.failure()
            }
        }

        val secretKeyResponse = notificationSettingsService
            .uploadPushNotificationDeviceConfigurationsToServer(
                serverUrl = serverConfigurationService.serverUrl.first(),
                authToken = authToken,
                firebaseToken = token
            )

        return when (secretKeyResponse) {
            // Network request failed, try again!
            is NetworkResponse.Failure -> Result.retry()
            is NetworkResponse.Response -> {
                notificationConfigurationService.storeAESKey(secretKeyResponse.data)
                Result.success()
            }
        }
    }
}