package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import de.tum.informatics.www1.artemis.native_app.core.common.defaultInternetWorkRequest
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationJobService
import kotlinx.coroutines.guava.await

internal class WorkManagerPushNotificationJobService(
    private val context: Context
) : PushNotificationJobService {

    private companion object {
        private const val UPLOAD_PUSH_NOTIFICATION_DEVICE_CONFIGURATION_WORKER_NAME =
            "upload_push_notification_device_configuration"
    }

    private val workManager: WorkManager get() = WorkManager.getInstance(context)

    override fun scheduleUploadPushNotificationDeviceConfigurationToServer() {
        val request = defaultInternetWorkRequest<UploadPushNotificationDeviceConfigurationWorker>(
            Data.Builder().build()
        )

        workManager
            .beginUniqueWork(
                UPLOAD_PUSH_NOTIFICATION_DEVICE_CONFIGURATION_WORKER_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
            .enqueue()
    }

    override suspend fun cancelPendingUploadPushNotificationDeviceConfigurationToServer() {
        workManager.cancelUniqueWork(UPLOAD_PUSH_NOTIFICATION_DEVICE_CONFIGURATION_WORKER_NAME)
            .result
            .await()
    }

    override fun scheduleUnsubscribeFromNotifications(
        serverUrl: String,
        authToken: String,
        firebaseToken: String
    ) {
        val request = defaultInternetWorkRequest<UnsubscribeFromNotificationsWorker>(
            Data
                .Builder()
                .putString(UnsubscribeFromNotificationsWorker.SERVER_URL_KEY, serverUrl)
                .putString(UnsubscribeFromNotificationsWorker.AUTH_TOKEN_KEY, authToken)
                .putString(UnsubscribeFromNotificationsWorker.FIREBASE_TOKEN_KEY, firebaseToken)
                .build()
        )

        workManager
            .beginUniqueWork(
                UPLOAD_PUSH_NOTIFICATION_DEVICE_CONFIGURATION_WORKER_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
            .enqueue()
    }
}
