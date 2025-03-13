package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager.mark_as_read

import android.content.Context
import android.content.Intent
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.push.service.CommunicationNotificationManager
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager.BaseCommunicationNotificationReceiver
import kotlinx.coroutines.runBlocking
import org.koin.core.component.get

/**
 * onReceive will be called by the mark as read action of the notification.
 * This receiver schedules a reply job to upload the reply.
 */
class MarkAsReadReceiver : BaseCommunicationNotificationReceiver() {

    override fun onReceive(parentId: Long, context: Context, intent: Intent) {
        val metisContext = runBlocking { getMetisContext(parentId) }
        enqueueWorker(metisContext, context)
        deleteNotification(parentId)
    }

    private fun enqueueWorker(
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