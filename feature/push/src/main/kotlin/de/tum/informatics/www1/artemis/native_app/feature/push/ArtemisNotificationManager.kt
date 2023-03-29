package de.tum.informatics.www1.artemis.native_app.feature.push

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.CoursePostTarget
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.ExercisePostTarget
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.ExerciseTarget
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.LecturePostTarget
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.MetisTarget
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.MiscNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.ArtemisNotification
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.CommunicationNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.NotificationTarget
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.NotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.ReplyPostCommunicationNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.ReplyPostCommunicationNotificationType.*
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.StandalonePostCommunicationNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.StandalonePostCommunicationNotificationType.*
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.UnknownNotificationTarget
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Handles the notifications.
 * To get the next notification id use getNextNotificationId.
 */
object ArtemisNotificationManager {

    private const val TAG = "ArtemisNotificationManager"

    private val LatestPushNotificationId = intPreferencesKey("latestPushNotificationId")

    private val Context.notificationDataStore by preferencesDataStore("push_notification_ids")

    suspend fun getNextNotificationId(context: Context): Int {
        val id =
            context.notificationDataStore.data.map { it[LatestPushNotificationId] ?: 0 }.first() + 1

        context.notificationDataStore.edit { data ->
            data[LatestPushNotificationId] = id
        }

        return id
    }



    fun popCommunicationNotification(
        context: Context,
        notificationType: CommunicationNotificationType,
        artemisNotification: ArtemisNotification
    ) {

    }

    private fun NotificationCompat.Builder.setNotificationTypeActions(
        context: Context,
        payload: String,
        target: NotificationTarget,
        notificationId: Int
    ) {
        when (target) {
            is MetisTarget -> {
                val replyText = context.getString(R.string.push_notification_action_reply)
                val remoteInput = RemoteInput
                    .Builder(ReplyReceiver.REPLY_INTENT_KEY)
                    .setLabel(replyText)
                    .build()

                val resultIntent =
                    Intent(context, ReplyReceiver::class.java).apply {
                        putExtra(
                            ReplyReceiver.REPLY_INTENT_NOTIFICATION_PAYLOAD,
                            payload
                        )
                        putExtra(
                            ReplyReceiver.REPLY_INTENT_NOTIFICATION_METIS_CONTEXT,
                            Json.encodeToString(target.metisContext)
                        )
                        putExtra(
                            ReplyReceiver.REPLY_INTENT_NOTIFICATION_POST_ID,
                            target.postId
                        )
                        putExtra(ReplyReceiver.REPLY_INTENT_NOTIFICATION_ID, notificationId)
                    }

                val flags = if (Build.VERSION.SDK_INT >= 31) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                } else PendingIntent.FLAG_UPDATE_CURRENT

                val replyPendingIntent: PendingIntent =
                    PendingIntent.getBroadcast(
                        context,
                        notificationId, // Set request code to notification id.
                        resultIntent,
                        flags
                    )

                val action = NotificationCompat.Action.Builder(
                    R.drawable.reply,
                    replyText,
                    replyPendingIntent
                )
                    .addRemoteInput(remoteInput)
                    .build()

                addAction(action)
            }

            else -> {}
        }
    }

    @Serializable
    private data class MessagePayload(
        val title: String? = null,
        val body: String? = null,
        val target: String? = null,
        val type: String? = null
    )
}