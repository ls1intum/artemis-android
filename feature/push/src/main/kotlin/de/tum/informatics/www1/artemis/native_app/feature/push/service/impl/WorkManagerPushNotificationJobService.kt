package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.await
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.KtorProvider
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationJobService
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import java.util.concurrent.TimeUnit
import javax.crypto.SecretKey

@OptIn(DelicateCoroutinesApi::class)
internal class WorkManagerPushNotificationJobService(
    private val context: Context,
    private val ktorProvider: KtorProvider
) :
    PushNotificationJobService {

    private companion object {
        private val FIREBASE_TOKEN_KEY = stringPreferencesKey("firebase_token")

        private const val UPLOAD_PUSH_NOTIFICATION_DEVICE_CONFIGURATION_WORKER_NAME =
            "upload_push_notification_device_configuration"
    }

    private val workManager: WorkManager get() = WorkManager.getInstance(context)

    private val Context.firebaseTokenDataStore by preferencesDataStore("firebase_token_store")

    override val firebaseToken: Flow<String?> = context.firebaseTokenDataStore.data.map { data ->
        data[FIREBASE_TOKEN_KEY]
    }.shareIn(GlobalScope, SharingStarted.Lazily, replay = 1)

    override suspend fun storeFirebaseToken(token: String) {
        context.firebaseTokenDataStore.edit { data ->
            data[FIREBASE_TOKEN_KEY] = token
        }
    }

    override suspend fun uploadPushNotificationDeviceConfigurationsToServer(
        serverUrl: String,
        authToken: String,
        aesKey: SecretKey,
        firebaseToken: String
    ): NetworkResponse<HttpStatusCode> {
        // TODO("Not yet implemented")
        return NetworkResponse.Response(HttpStatusCode.OK)
    }

    override suspend fun unsubscribeFromNotifications(
        serverUrl: String,
        authToken: String
    ): NetworkResponse<HttpStatusCode> {
        // TODO("Not yet implemented")
        return NetworkResponse.Response(HttpStatusCode.OK)
    }

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