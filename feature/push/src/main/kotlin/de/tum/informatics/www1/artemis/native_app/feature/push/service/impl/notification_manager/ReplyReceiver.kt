package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import androidx.room.withTransaction
import androidx.work.OneTimeWorkRequestBuilder
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.CreatePostService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.work.BaseCreatePostWorker
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.push.PushCommunicationDatabaseProvider
import de.tum.informatics.www1.artemis.native_app.feature.push.service.CommunicationNotificationManager
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/**
 * onReceive will be called by the reply action of the notification.
 * This receiver schedules a reply job to upload the reply.
 */
class ReplyReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        const val REPLY_INTENT_KEY = "reply_text_key"
        const val PARENT_ID = "parent_id"
    }

    @SuppressLint("EnqueueWork")
    override fun onReceive(context: Context, intent: Intent) {
        RemoteInput.getResultsFromIntent(intent)?.let { remoteInput ->
            val response =
                remoteInput.getCharSequence(REPLY_INTENT_KEY)
                    .toString()

            val parentId = intent.getLongExtra(PARENT_ID, 0)

            val pushCommunicationDatabaseProvider: PushCommunicationDatabaseProvider = get()

            val (metisContext: MetisContext, postId: Long) = runBlocking {
                pushCommunicationDatabaseProvider.database.withTransaction {
                    val communication =
                        pushCommunicationDatabaseProvider.pushCommunicationDao.getCommunication(parentId)

                    val metisTarget = NotificationTargetManager.getCommunicationNotificationTarget(
                        communication.target
                    )
                    metisTarget.metisContext to metisTarget.postId
                }
            }

            if (response.isNotBlank() && metisContext is MetisContext.Conversation) {
                val createPostService: CreatePostService = get()
                createPostService.createAnswerPost(
                    metisContext.courseId,
                    metisContext.conversationId,
                    postId,
                    response
                ) { clientSidePostId ->
                    then(
                        OneTimeWorkRequestBuilder<UpdateReplyNotificationWorker>()
                            .setInputData(
                                BaseCreatePostWorker.createWorkInput(
                                    metisContext.courseId,
                                    metisContext.conversationId,
                                    clientSidePostId,
                                    response,
                                    hasForwardedMessage = false,
                                    postType = BaseCreatePostWorker.PostType.ANSWER_POST,
                                    parentPostId = postId
                                )
                            )
                            .build()
                    )
                }
            }

            val communicationNotificationManager: CommunicationNotificationManager = get()

            // Repop the notification to tell the OS we handled the notification
            runBlocking {
                communicationNotificationManager.repopNotification(parentId = parentId)
            }
        }
    }
}