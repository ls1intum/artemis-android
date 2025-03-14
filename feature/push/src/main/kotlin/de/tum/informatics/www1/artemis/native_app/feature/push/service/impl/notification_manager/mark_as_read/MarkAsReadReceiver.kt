package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager.mark_as_read

import android.content.Context
import android.content.Intent
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.push.communication_notification_model.PushCommunicationEntity
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.StandalonePostCommunicationNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.service.CommunicationNotificationManager
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager.BaseCommunicationNotificationReceiver
import kotlinx.coroutines.runBlocking
import org.koin.core.component.get

/**
 * onReceive will be called by the mark as read action of the notification.
 * This receiver schedules a reply job to upload the reply.
 */
class MarkAsReadReceiver : BaseCommunicationNotificationReceiver() {

    override fun onReceive(
        communicationEntity: PushCommunicationEntity,
        context: Context,
        intent: Intent
    ) {
        // New thread replies do not increase a conversation's unread count, so we don't need to mark them as read.
        if (communicationEntity.notificationType is StandalonePostCommunicationNotificationType) {
            enqueueMarkConversationAsReadWorker(
                metisContext = communicationEntity.target.metisContext,
                context = context
            )
        }
        deleteNotification(communicationEntity.parentId)
    }

    private fun enqueueMarkConversationAsReadWorker(
        metisContext: MetisContext.Conversation,
        context: Context
    ) {
        val markAsReadWorkRequest = OneTimeWorkRequestBuilder<MarkConversationAsReadWorker>()
            .setInputData(
                workDataOf(
                    MarkConversationAsReadWorker.KEY_COURSE_ID to metisContext.courseId,
                    MarkConversationAsReadWorker.KEY_CONVERSATION_ID to metisContext.conversationId
                )
            )
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )
            .build()

        WorkManager.getInstance(context).enqueue(markAsReadWorkRequest)
    }

    private fun deleteNotification(parentId: Long) {
        val communicationNotificationManager: CommunicationNotificationManager = get()
        runBlocking {
            communicationNotificationManager.deleteCommunication(parentId = parentId)
        }
    }
}