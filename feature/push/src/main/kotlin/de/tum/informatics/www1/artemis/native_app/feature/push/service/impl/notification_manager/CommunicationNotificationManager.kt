package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.room.withTransaction
import de.tum.informatics.www1.artemis.native_app.feature.push.ArtemisNotificationChannel
import de.tum.informatics.www1.artemis.native_app.feature.push.ArtemisNotificationManager
import de.tum.informatics.www1.artemis.native_app.feature.push.PushCommunicationDatabaseProvider
import de.tum.informatics.www1.artemis.native_app.feature.push.R
import de.tum.informatics.www1.artemis.native_app.feature.push.communication_notification_model.CommunicationMessageEntity
import de.tum.informatics.www1.artemis.native_app.feature.push.communication_notification_model.CommunicationType
import de.tum.informatics.www1.artemis.native_app.feature.push.communication_notification_model.PushCommunicationEntity
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.ArtemisNotification
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.CommunicationNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.communicationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.parentId
import kotlinx.coroutines.runBlocking

/**
 * Handles communication notifications and pops them as communication style push notifications.
 * Uses a database to store previously sent notification.
 */
internal class CommunicationNotificationManager(
    private val context: Context,
    private val dbProvider: PushCommunicationDatabaseProvider
) : BaseNotificationManager {

    fun popNotification(
        artemisNotification: ArtemisNotification<CommunicationNotificationType>
    ) {
        val (communication, messages) = runBlocking {
            dbProvider.database.withTransaction {
                dbProvider.pushCommunicationDao.insertNotification(artemisNotification) {
                    ArtemisNotificationManager.getNextNotificationId(context)
                }

                val parentId = artemisNotification.parentId
                val communicationType = artemisNotification.communicationType

                val communication = dbProvider.pushCommunicationDao.getCommunication(
                    parentId,
                    communicationType
                )

                val messages = dbProvider.pushCommunicationDao.getCommunicationMessages(
                    parentId,
                    communicationType
                )

                communication to messages
            }
        }

        if (messages.isEmpty()) return
        popCommunicationNotification(communication, messages)
    }

    /**
     * Reads the notification from the database and pops the notification
     */
    suspend fun repopNotification(
        parentId: Long,
        communicationType: CommunicationType
    ) {
        val (communication, messages) = dbProvider.database.withTransaction {
            val communication = dbProvider.pushCommunicationDao.getCommunication(
                parentId,
                communicationType
            )

            val messages = dbProvider.pushCommunicationDao.getCommunicationMessages(
                parentId,
                communicationType
            )

            communication to messages
        }

        popCommunicationNotification(communication, messages)
    }

    private fun popCommunicationNotification(
        communication: PushCommunicationEntity,
        messages: List<CommunicationMessageEntity>
    ) {
        val notificationChannel: ArtemisNotificationChannel = communication.type.notificationChannel

        val metisTarget = NotificationTargetManager.getCommunicationNotificationTarget(
            communication.type,
            communication.target
        )

        val notification = NotificationCompat.Builder(context, notificationChannel.id)
            .setStyle(buildMessagingStyle(communication, messages))
            .setSmallIcon(communication.type.notificationIcon)
            .setContentIntent(
                NotificationTargetManager.getMetisContentIntent(
                    context = context,
                    notificationTarget = metisTarget
                )
            )
            .setDeleteIntent(
                constructDeletionIntent(
                    context = context,
                    parentId = communication.parentId,
                    type = communication.type
                )
            )
            .addAction(
                buildDirectReplyAction(
                    context = context,
                    communication = communication,
                    notificationId = communication.notificationId
                )
            )
            .build()

        popNotification(context, notification, communication.notificationId)
    }


    fun deleteCommunication(parentId: Long, type: CommunicationType) {
        runBlocking {
            dbProvider.pushCommunicationDao.deleteCommunication(parentId, type)
        }
    }

    private fun buildMessagingStyle(
        communication: PushCommunicationEntity,
        messages: List<CommunicationMessageEntity>
    ): NotificationCompat.MessagingStyle {
        val person = when (communication.type) {
            CommunicationType.QNA_COURSE, CommunicationType.QNA_EXERCISE, CommunicationType.QNA_LECTURE, CommunicationType.ANNOUNCEMENT -> {
                val firstMessage = messages.first()
                Person.Builder().setName(firstMessage.authorName).build()
            }
        }

        val style = NotificationCompat.MessagingStyle(person)
        messages.forEach { message ->
            style.addMessage(
                NotificationCompat.MessagingStyle.Message(
                    message.text,
                    message.date.toEpochMilliseconds(),
                    Person.Builder()
                        .setName(message.authorName)
                        .build()
                )
            )
        }

        when (communication.type) {
            CommunicationType.QNA_COURSE, CommunicationType.QNA_EXERCISE, CommunicationType.QNA_LECTURE, CommunicationType.ANNOUNCEMENT -> {
                style.isGroupConversation = true
                style.conversationTitle = when (communication.type) {
                    CommunicationType.QNA_COURSE, CommunicationType.ANNOUNCEMENT -> context.getString(
                        R.string.conversation_title_course_post,
                        communication.courseTitle,
                        communication.title
                    )

                    CommunicationType.QNA_EXERCISE -> context.getString(
                        R.string.conversation_title_exercise_post,
                        communication.courseTitle,
                        communication.containerTitle,
                        communication.title
                    )

                    CommunicationType.QNA_LECTURE -> context.getString(
                        R.string.conversation_title_lecture_post,
                        communication.courseTitle,
                        communication.containerTitle,
                        communication.title
                    )
                }
            }
        }

        return style
    }

    private fun constructDeletionIntent(
        context: Context,
        parentId: Long,
        type: CommunicationType
    ): PendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, DeleteNotificationReceiver::class.java)
            .putExtra(DeleteNotificationReceiver.PARENT_ID, parentId)
            .putExtra(DeleteNotificationReceiver.COMMUNICATION_TYPE, type.name),
        PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private fun buildDirectReplyAction(
        context: Context,
        communication: PushCommunicationEntity,
        notificationId: Int
    ): NotificationCompat.Action {
        val replyText = context.getString(R.string.push_notification_action_reply)
        val remoteInput = RemoteInput
            .Builder(ReplyReceiver.REPLY_INTENT_KEY)
            .setLabel(replyText)
            .build()

        val resultIntent =
            Intent(context, ReplyReceiver::class.java).apply {
                putExtra(
                    ReplyReceiver.PARENT_ID,
                    communication.parentId
                )
                putExtra(
                    ReplyReceiver.COMMUNICATION_TYPE,
                    communication.type.name
                )
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

        return NotificationCompat.Action.Builder(
            R.drawable.reply,
            replyText,
            replyPendingIntent
        )
            .addRemoteInput(remoteInput)
            .build()
    }
}