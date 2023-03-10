package de.tum.informatics.www1.artemis.native_app.feature.push

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Handles the notifications.
 * To get the next notification id use getNextNotificationId.
 */
object ArtemisNotificationManager {

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

    /**
     * Pop a notification based on the decrypted payload received from the server.
     */
    fun popNotification(context: Context, payload: String, notificationId: Int) {
        val messagePayload: MessagePayload = Json.decodeFromString(payload)
        val title = messagePayload.title
        val body = messagePayload.body

        val type = try {
            NotificationType.valueOf(messagePayload.type ?: return)
        } catch (e: IllegalArgumentException) {
            return
        }

        val target = getNotificationTarget(type, messagePayload.target ?: return)

        val notification = NotificationCompat.Builder(context, ArtemisNotificationChannel.id)
            .apply {
                if (title != null) setContentTitle(title)
                if (body != null) setContentText(body)

                setSmallIcon(R.drawable.push_notification_icon)
                setContentIntent(buildOnClickIntent(context, target, type))
                setNotificationTypeActions(context, payload, target, notificationId)
                setAutoCancel(true)
            }
            .build()

        NotificationManagerCompat
            .from(context)
            .notify(notificationId, notification)
    }

    private fun getNotificationTarget(type: NotificationType, target: String): NotificationTarget {
        return when (type) {
            NotificationType.NEW_REPLY_FOR_COURSE_POST, NotificationType.NEW_COURSE_POST, NotificationType.NEW_ANNOUNCEMENT_POST -> {
                Json.decodeFromString<CoursePostTarget>(target)
            }
            NotificationType.NEW_LECTURE_POST, NotificationType.NEW_REPLY_FOR_LECTURE_POST -> {
                Json.decodeFromString<LecturePostTarget>(target)
            }
            NotificationType.NEW_EXERCISE_POST, NotificationType.NEW_REPLY_FOR_EXERCISE_POST -> {
                Json.decodeFromString<ExercisePostTarget>(target)
            }
            NotificationType.QUIZ_EXERCISE_STARTED -> {
                Json.decodeFromString<ExerciseTarget>(target)
            }
            else -> UnknownNotificationTarget
        }
    }

    private fun buildOnClickIntent(
        context: Context,
        notificationTarget: NotificationTarget,
        type: NotificationType
    ): PendingIntent {
        val mainActivity =
            Class.forName("de.tum.informatics.www1.artemis.native_app.android.ui.MainActivity")

        val openAppIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, mainActivity),
            PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val uriString: String? = when (notificationTarget) {
                is CoursePostTarget -> {
                    "artemis://metis_standalone_post/${notificationTarget.postId}/${notificationTarget.courseId}/null/null"
                }
                is LecturePostTarget -> {
                    "artemis://metis_standalone_post/${notificationTarget.postId}/${notificationTarget.courseId}/null/${notificationTarget.lectureId}"
                }
                is ExercisePostTarget -> {
                    "artemis://metis_standalone_post/${notificationTarget.postId}/${notificationTarget.courseId}/${notificationTarget.exerciseId}/null"
                }
                is ExerciseTarget -> {
                    if (type == NotificationType.QUIZ_EXERCISE_STARTED) {
                        "artemis://quiz_participation/${notificationTarget.courseId}/${notificationTarget.exerciseId}"
                    } else null
                }
                else -> null
            }

            return if (uriString != null) {
                PendingIntent.getActivity(
                    context,
                    0,
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(uriString)
                    ),
                    PendingIntent.FLAG_IMMUTABLE
                )
            } else openAppIntent
        } catch (e: Exception) {
            return openAppIntent
        }
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