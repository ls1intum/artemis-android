package de.tum.informatics.www1.artemis.native_app.feature.push

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.work.Data
import androidx.work.WorkManager

/**
 * onReceive will be called by the reply action of the notification.
 * This receiver schedules a reply job to upload the reply.
 */
class ReplyReceiver : BroadcastReceiver() {

    companion object {
        const val REPLY_INTENT_KEY = "reply_text_key"
        const val REPLY_INTENT_NOTIFICATION_PAYLOAD = "payload"
        const val REPLY_INTENT_NOTIFICATION_METIS_CONTEXT = "metis_context"
        const val REPLY_INTENT_NOTIFICATION_POST_ID = "postId"
        const val REPLY_INTENT_NOTIFICATION_ID = "notification_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        RemoteInput.getResultsFromIntent(intent)?.let { remoteInput ->
            val response =
                remoteInput.getCharSequence(REPLY_INTENT_KEY)
                    .toString()

            val notificationId: Int =
                intent.getIntExtra(REPLY_INTENT_NOTIFICATION_ID, 0)

            val notificationManager = NotificationManagerCompat
                .from(context)

            val payload = intent.getStringExtra(REPLY_INTENT_NOTIFICATION_PAYLOAD)
            val metisContextString = intent.getStringExtra(REPLY_INTENT_NOTIFICATION_METIS_CONTEXT) ?: return

            if (response.isNotBlank() && !payload.isNullOrBlank()) {
                val postId: Long =
                    intent.getLongExtra(
                        REPLY_INTENT_NOTIFICATION_POST_ID,
                        0L
                    )


                val workRequest = defaultInternetWorkRequest<ReplyWorker>(
                    Data.Builder()
                        .putString(ReplyWorker.KEY_METIS_CONTEXT, metisContextString)
                        .putLong(ReplyWorker.KEY_POST_ID, postId)
                        .putString(ReplyWorker.KEY_REPLY_CONTENT, response)
                        .build()
                )

                WorkManager.getInstance(context)
                    .enqueue(workRequest)

                ArtemisNotificationManager.popNotification(
                    context = context,
                    payload = payload,
                    notificationId = notificationId
                )
            } else {
                notificationManager.cancel(notificationId)
            }
        }
    }
}