package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager.mute

import android.content.Context
import android.content.Intent
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.push.communication_notification_model.PushCommunicationEntity
import de.tum.informatics.www1.artemis.native_app.feature.push.service.CommunicationNotificationManager
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager.BaseCommunicationNotificationReceiver
import kotlinx.coroutines.runBlocking
import org.koin.core.component.get

class MuteConversationReceiver : BaseCommunicationNotificationReceiver() {

    override fun onReceive(
        communicationEntity: PushCommunicationEntity,
        context: Context,
        intent: Intent
    ) {
        enqueueMuteConversationWorker(
            metisContext = communicationEntity.target.metisContext,
            context = context
        )
        deleteNotification(communicationEntity.parentId)
    }

    private fun enqueueMuteConversationWorker(
        metisContext: MetisContext.Conversation,
        context: Context
    ) {
        val muteWorkRequest = OneTimeWorkRequestBuilder<MuteConversationWorker>()
            .setInputData(
                workDataOf(
                    MuteConversationWorker.KEY_COURSE_ID to metisContext.courseId,
                    MuteConversationWorker.KEY_CONVERSATION_ID to metisContext.conversationId
                )
            )
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )
            .build()

        WorkManager.getInstance(context).enqueue(muteWorkRequest)
    }

    private fun deleteNotification(parentId: Long) {
        val communicationNotificationManager: CommunicationNotificationManager = get()
        runBlocking {
            communicationNotificationManager.deleteCommunication(parentId = parentId)
        }
    }
}
