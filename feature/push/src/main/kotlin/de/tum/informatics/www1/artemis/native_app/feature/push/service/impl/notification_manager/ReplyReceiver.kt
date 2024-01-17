package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import androidx.work.Data
import androidx.work.WorkManager
import de.tum.informatics.www1.artemis.native_app.feature.push.communication_notification_model.CommunicationType
import de.tum.informatics.www1.artemis.native_app.feature.push.defaultInternetWorkRequest
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
        const val COMMUNICATION_TYPE = "communication_type"
    }

    override fun onReceive(context: Context, intent: Intent) {
        RemoteInput.getResultsFromIntent(intent)?.let { remoteInput ->
            val response =
                remoteInput.getCharSequence(REPLY_INTENT_KEY)
                    .toString()

            val parentId = intent.getLongExtra(PARENT_ID, 0)
            val communicationType = intent.getStringExtra(COMMUNICATION_TYPE) ?: return@let

            if (response.isNotBlank()) {
                val workRequest = defaultInternetWorkRequest<SendConversationPostWorker>(
                    Data.Builder()
                        .putLong(SendConversationPostWorker.KEY_PARENT_ID, parentId)
                        .putString(SendConversationPostWorker.KEY_COMMUNICATION_TYPE, communicationType)
                        .putString(SendConversationPostWorker.KEY_REPLY_CONTENT, response)
                        .build()
                )

                WorkManager.getInstance(context)
                    .enqueue(workRequest)
            }

            val communicationNotificationManager: CommunicationNotificationManager = get()

            // Repop the notification to tell the OS we handled the notification
            runBlocking {
                communicationNotificationManager.repopNotification(
                    parentId = parentId,
                    communicationType = CommunicationType.valueOf(communicationType)
                )
            }
        }
    }
}