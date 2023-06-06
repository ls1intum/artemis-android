package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager

import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.room.withTransaction
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.onSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.service.ServerDataService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisModificationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.AnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.ConversationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.getConversation
import de.tum.informatics.www1.artemis.native_app.feature.push.ArtemisNotificationChannel
import de.tum.informatics.www1.artemis.native_app.feature.push.ArtemisNotificationManager
import de.tum.informatics.www1.artemis.native_app.feature.push.PushCommunicationDatabaseProvider
import de.tum.informatics.www1.artemis.native_app.feature.push.R
import de.tum.informatics.www1.artemis.native_app.feature.push.communication_notification_model.CommunicationType
import de.tum.informatics.www1.artemis.native_app.feature.push.service.CommunicationNotificationManager
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock

/**
 * Worker that sends a reply to a Metis posting to the server.
 * If the input is invalid, the worker fails.
 * If the input is valid, but the reply could not be uploaded, the worker will schedule a retry.
 * If the upload failed 5 times, the worker will fail and pop a notification to notify the user about the failure.
 */
internal class ReplyWorker(
    appContext: Context,
    params: WorkerParameters,
    private val metisModificationService: MetisModificationService,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
    private val pushCommunicationDatabaseProvider: PushCommunicationDatabaseProvider,
    private val communicationNotificationManager: CommunicationNotificationManager,
    private val serverDataService: ServerDataService,
    private val conversationService: ConversationService
) :
    CoroutineWorker(appContext, params) {

    companion object {
        const val KEY_PARENT_ID = "parent_id"
        const val KEY_COMMUNICATION_TYPE = "communication_type"
        const val KEY_REPLY_CONTENT = "reply_content"

        private const val TAG = "ReplyWorker"
    }

    override suspend fun doWork(): Result {
        val parentId: Long = inputData.getLong(KEY_PARENT_ID, 0)
        val communicationType: CommunicationType = CommunicationType.valueOf(
            inputData.getString(
                KEY_COMMUNICATION_TYPE
            ) ?: return Result.failure()
        )

        val (metisContext: MetisContext, postId: Long) = pushCommunicationDatabaseProvider.database.withTransaction {
            val communication =
                pushCommunicationDatabaseProvider.pushCommunicationDao.getCommunication(
                    parentId,
                    communicationType
                )

            val metisTarget = NotificationTargetManager.getCommunicationNotificationTarget(
                communication.type,
                communication.target
            )
            metisTarget.metisContext to metisTarget.postId
        }

        val replyContent = inputData.getString(KEY_REPLY_CONTENT) ?: return Result.failure()

        val errorReturnType = if (runAttemptCount > 5) {
            popFailureNotification(replyContent)
            Result.failure()
        } else Result.retry()

        return when (val authData = accountService.authenticationData.first()) {
            is AccountService.AuthenticationData.LoggedIn -> {
                val serverUrl = serverConfigurationService.serverUrl.first()

                val conversation = when (metisContext) {
                    is MetisContext.Conversation -> conversationService
                        .getConversation(
                            metisContext.courseId,
                            metisContext.conversationId,
                            authData.authToken,
                            serverUrl
                        ).orNull() ?: return errorReturnType

                    else -> null
                }

                val time = Clock.System.now()

                metisModificationService.createAnswerPost(
                    metisContext,
                    AnswerPost(
                        content = replyContent,
                        post = StandalonePost(
                            id = postId,
                            conversation = conversation
                        ),
                        creationDate = Clock.System.now()
                    ),
                    serverUrl = serverUrl,
                    authToken = authData.authToken
                )
                    .onSuccess {
                        // We can add to the notification that the user has responded. However, this does not have super high priority
                        val accountData = serverDataService.getAccountData(
                            serverUrl,
                            authData.authToken
                        )

                        if (accountData is NetworkResponse.Response) {
                            val authorName =
                                "${accountData.data.firstName} ${accountData.data.lastName}"
                            communicationNotificationManager.addSelfMessage(
                                parentId = parentId,
                                type = communicationType,
                                authorName = authorName,
                                body = replyContent,
                                date = time
                            )
                        }
                    }
                    .bind { Result.success() }
                    .or(errorReturnType)
            }

            AccountService.AuthenticationData.NotLoggedIn -> Result.failure()
        }
    }

    private suspend fun popFailureNotification(replyContent: String) {
        val id = ArtemisNotificationManager.getNextNotificationId(applicationContext)

        val notification =
            NotificationCompat.Builder(
                applicationContext,
                ArtemisNotificationChannel.MiscNotificationChannel.id
            )
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

        try {
            NotificationManagerCompat
                .from(applicationContext)
                .notify(id, notification)
        } catch (e: SecurityException) {
            Log.e(TAG, "Could not push reply notification due to missing permission")
        }
    }
}