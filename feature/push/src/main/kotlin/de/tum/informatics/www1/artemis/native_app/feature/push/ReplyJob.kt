package de.tum.informatics.www1.artemis.native_app.feature.push

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisContext
import de.tum.informatics.www1.artemis.native_app.core.model.metis.AnswerPost
import de.tum.informatics.www1.artemis.native_app.core.model.metis.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisService
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class ReplyJob(
    appContext: Context,
    params: WorkerParameters,
    private val metisService: MetisService,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService
) :
    CoroutineWorker(appContext, params) {

    companion object {
        const val KEY_METIS_CONTEXT = "metis_context"
        const val KEY_POST_ID = "post_id"
        const val KEY_REPLY_CONTENT = "reply_content"
    }

    override suspend fun doWork(): Result {
        val metisContext: MetisContext =
            Json.decodeFromString(
                inputData.getString(KEY_METIS_CONTEXT) ?: return Result.failure()
            )

        val postId: Long = inputData.getLong(KEY_POST_ID, 0L)

        val replyContent = inputData.getString(KEY_REPLY_CONTENT) ?: return Result.failure()

        return when (val authData = accountService.authenticationData.first()) {
            is AccountService.AuthenticationData.LoggedIn -> {
                val response = metisService.createAnswerPost(
                    metisContext,
                    AnswerPost(
                        content = replyContent,
                        post = StandalonePost(
                            id = postId
                        ),
                        creationDate = Clock.System.now()
                    ),
                    serverUrl = serverConfigurationService.serverUrl.first(),
                    authToken = authData.authToken
                )

                when (response) {
                    is NetworkResponse.Response -> Result.success()
                    is NetworkResponse.Failure -> {
                        if (runAttemptCount > 5) {
                            popFailureNotification(replyContent)
                            Result.failure()
                        } else Result.retry()
                    }
                }
            }
            AccountService.AuthenticationData.NotLoggedIn -> Result.failure()
        }
    }

    private suspend fun popFailureNotification(replyContent: String) {
        val id = ArtemisNotificationManager.getNextNotificationId(applicationContext)

        val notification =
            NotificationCompat.Builder(applicationContext, ArtemisNotificationChannel.id)
                .setSmallIcon(R.drawable.push_notification_icon)
                .setContentTitle(applicationContext.getString(R.string.push_notification_send_reply_failed_title))
                .setContentText(
                    applicationContext.getString(
                        R.string.push_notification_send_reply_failed_message,
                        replyContent
                    )
                )
                .setAutoCancel(true)
                .build()

        NotificationManagerCompat
            .from(applicationContext)
            .notify(id, notification)
    }
}