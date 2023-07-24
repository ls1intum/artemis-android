package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.tum.informatics.www1.artemis.native_app.core.data.onFailure
import de.tum.informatics.www1.artemis.native_app.core.data.onSuccess
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationConfigurationService
import de.tum.informatics.www1.artemis.native_app.feature.push.service.network.NotificationSettingsService
import io.ktor.http.isSuccess

internal class UnsubscribeFromNotificationsWorker(
    appContext: Context,
    params: WorkerParameters,
    private val notificationSettingsService: NotificationSettingsService
) :
    CoroutineWorker(appContext, params) {

    internal companion object {
        const val SERVER_URL_KEY = "server_url"
        const val AUTH_TOKEN_KEY = "auth_token"
        const val FIREBASE_TOKEN_KEY = "firebase_token"

        private const val TAG = "UnsubscribeFromNotificationsWorker"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Executing unsubscribe job")

        val serverUrl = inputData.getString(SERVER_URL_KEY)
        val authToken = inputData.getString(AUTH_TOKEN_KEY)
        val firebaseToken = inputData.getString(FIREBASE_TOKEN_KEY)
        if (serverUrl == null || authToken == null || firebaseToken == null) return Result.failure()

        return notificationSettingsService
            .unsubscribeFromNotifications(serverUrl, authToken, firebaseToken)
            .onSuccess {
                if (it.isSuccess()) {
                    Log.d(TAG, "Unsubscribed from notifications")
                } else {
                    Log.d(TAG, "Failed unsubscribing with status code=$it")
                }
            }
            .onFailure {
                Log.d(TAG, "Failed unsubscribing")
            }
            .toWorkerResult()
    }
}