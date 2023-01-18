package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import io.ktor.http.isSuccess

internal class UnsubscribeFromNotificationsWorker(
    appContext: Context,
    params: WorkerParameters,
    private val pushNotificationJobService: WorkManagerPushNotificationJobService
) :
    CoroutineWorker(appContext, params) {

    internal companion object {
        const val SERVER_URL_KEY = "server_url"
        const val AUTH_TOKEN_KEY = "auth_token"
    }

    override suspend fun doWork(): Result {
        val serverUrl = inputData.getString(SERVER_URL_KEY)
        val authToken = inputData.getString(AUTH_TOKEN_KEY)
        if (serverUrl == null || authToken == null) return Result.failure()

        return pushNotificationJobService
            .unsubscribeFromNotifications(serverUrl, authToken)
            .toWorkerResult()
    }
}