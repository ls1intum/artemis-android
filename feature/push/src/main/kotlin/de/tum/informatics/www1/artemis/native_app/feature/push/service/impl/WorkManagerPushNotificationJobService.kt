package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl

import android.content.Context
import androidx.work.*
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.KtorProvider
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationJobService
import java.util.concurrent.TimeUnit

internal class WorkManagerPushNotificationJobService(
    private val context: Context,
    private val ktorProvider: KtorProvider
) : PushNotificationJobService {

    private companion object {
        private const val UPLOAD_PUSH_NOTIFICATION_DEVICE_CONFIGURATION_WORKER_NAME =
            "upload_push_notification_device_configuration"
    }

    private val workManager: WorkManager get() = WorkManager.getInstance(context)

    override fun scheduleUploadPushNotificationDeviceConfigurationToServer() {
        val request = defaultInternetWorkRequest<UploadPushNotificationDeviceConfigurationWorker>()

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

    override fun scheduleUnsubscribeFromNotifications(serverUrl: String, authToken: String) {
        val request = defaultInternetWorkRequest<UnsubscribeFromNotificationsWorker>()

        workManager
            .beginUniqueWork(
                UPLOAD_PUSH_NOTIFICATION_DEVICE_CONFIGURATION_WORKER_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
            .enqueue()
    }

    private inline fun <reified T : ListenableWorker> defaultInternetWorkRequest(): OneTimeWorkRequest {
        return OneTimeWorkRequestBuilder<T>()
            // Only run when the device is connected to the internet.
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
    }
}