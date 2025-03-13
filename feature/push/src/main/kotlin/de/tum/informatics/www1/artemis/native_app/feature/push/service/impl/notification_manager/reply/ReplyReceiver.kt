package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager.reply

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import androidx.work.OneTimeWorkRequestBuilder
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.CreatePostService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.work.BaseCreatePostWorker
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager.BaseCommunicationNotificationReceiver
import kotlinx.coroutines.runBlocking
import org.koin.core.component.get

/**
 * onReceive will be called by the reply action of the notification.
 * This receiver schedules a reply job to upload the reply.
 */
class ReplyReceiver : BaseCommunicationNotificationReceiver() {

    companion object {
        const val REPLY_INTENT_KEY = "reply_text_key"
    }

    @SuppressLint("EnqueueWork")
    override fun onReceive(parentId: Long, context: Context, intent: Intent) {
        val remoteInput = RemoteInput.getResultsFromIntent(intent) ?: return
        val response = remoteInput.getCharSequence(REPLY_INTENT_KEY).toString()

        val (metisContext: MetisContext.Conversation, postId: Long) = runBlocking {
            getMetisContextAndPostId(parentId)
        }

        if (response.isNotBlank()) {
            createReplyPost(
                metisContext = metisContext,
                parentPostId = postId,
                response = response
            )
        }

        // Repop the notification to tell the OS we handled the notification
        runBlocking {
            communicationNotificationManager.repopNotification(parentId = parentId)
        }
    }

    @SuppressLint("EnqueueWork")        // This is already done in CreatePostServiceImpl.scheduleCreatePostWork
    private fun createReplyPost(
        metisContext: MetisContext.Conversation,
        parentPostId: Long,
        response: String
    ) {
        val createPostService: CreatePostService = get()
        createPostService.createAnswerPost(
            courseId = metisContext.courseId,
            conversationId = metisContext.conversationId,
            parentPostId = parentPostId,
            content = response
        ) { clientSidePostId ->
            then(
                OneTimeWorkRequestBuilder<UpdateReplyNotificationWorker>()
                    .setInputData(
                        BaseCreatePostWorker.createWorkInput(
                            metisContext.courseId,
                            metisContext.conversationId,
                            clientSidePostId,
                            response,
                            postType = BaseCreatePostWorker.PostType.ANSWER_POST,
                            parentPostId = parentPostId
                        )
                    )
                    .build()
            )
        }
    }
}