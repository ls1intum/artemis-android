package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationConfigurationService

internal class UnsubscribeFromNotificationsWorker(
    appContext: Context,
    params: WorkerParameters,
    private val pushNotificationConfigurationService: PushNotificationConfigurationService
) :
    CoroutineWorker(appContext, params) {

    internal companion object {
        const val SERVER_URL_KEY = "server_url"
        const val AUTH_TOKEN_KEY = "auth_token"
        const val FIREBASE_TOKEN_KEY = "auth_token"
    }

    override suspend fun doWork(): Result {
        val serverUrl = inputData.getString(SERVER_URL_KEY)
        val authToken = inputData.getString(AUTH_TOKEN_KEY)
        val firebaseToken = inputData.getString(FIREBASE_TOKEN_KEY)
        if (serverUrl == null || authToken == null || firebaseToken == null) return Result.failure()

        return pushNotificationConfigurationService
            .unsubscribeFromNotifications(serverUrl, authToken, firebaseToken)
            .toWorkerResult()
    }
}