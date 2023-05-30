package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
import de.tum.informatics.www1.artemis.native_app.feature.push.service.CommunicationNotificationManager
import kotlinx.datetime.Instant

/**
 * Handles communication notifications and pops them as communication style push notifications.
 * Uses a database to store previously sent notification.
 */
internal class CommunicationNotificationManagerImpl(
    private val context: Context,
    private val dbProvider: PushCommunicationDatabaseProvider
) : CommunicationNotificationManager, BaseNotificationManager {

    override suspend fun popNotification(
        artemisNotification: ArtemisNotification<CommunicationNotificationType>
    ) {
        val (communication, messages) = dbProvider.database.withTransaction {
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


        if (messages.isEmpty()) return
        popCommunicationNotification(communication, messages)
    }

    /**
     * Add a message to the notification sent by the user themself using direct reply.
     */
    override suspend fun addSelfMessage(
        parentId: Long,
        type: CommunicationType,
        authorName: String,
        body: String,
        date: Instant
    ) {
        dbProvider.pushCommunicationDao.insertSelfMessage(parentId, type, authorName, body, date)
        repopNotification(parentId, type)
    }

    override suspend fun repopNotification(
        parentId: Long,
        communicationType: CommunicationType
    ) {
        val (communication, messages) = dbProvider.database.withTransaction {
            if (!dbProvider.pushCommunicationDao.hasPushCommunication(
                    parentId,
                    communicationType
                )
            ) return@withTransaction null to null

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

        if (communication != null && messages != null) {
            popCommunicationNotification(communication, messages)
        }
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
            .setAutoCancel(true)
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
            .addAction(buildMarkAsReadAction(context = context, communication = communication))
            .build()

        popNotification(context, notification, communication.notificationId)
    }


    override suspend fun deleteCommunication(parentId: Long, type: CommunicationType) {
        val notificationId = dbProvider.database.withTransaction {
            if (!dbProvider.pushCommunicationDao.hasPushCommunication(
                    parentId,
                    type
                )
            ) return@withTransaction null

            val communication = dbProvider.pushCommunicationDao.getCommunication(parentId, type)
            dbProvider.pushCommunicationDao.deleteCommunication(parentId, type)
            communication.notificationId
        }

        if (notificationId != null) {
            NotificationManagerCompat
                .from(context)
                .cancel(notificationId)
        }
    }

    private fun buildMessagingStyle(
        communication: PushCommunicationEntity,
        messages: List<CommunicationMessageEntity>
    ): NotificationCompat.MessagingStyle {
        val person = when (communication.type) {
            CommunicationType.QNA_COURSE, CommunicationType.QNA_EXERCISE, CommunicationType.QNA_LECTURE, CommunicationType.ANNOUNCEMENT, CommunicationType.COMMUNICATION -> {
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
            CommunicationType.QNA_COURSE, CommunicationType.QNA_EXERCISE, CommunicationType.QNA_LECTURE, CommunicationType.ANNOUNCEMENT, CommunicationType.COMMUNICATION -> {
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

                    CommunicationType.COMMUNICATION -> if (communication.title != null) {
                        context.getString(
                            R.string.conversation_title_conversation_thread,
                            communication.courseTitle,
                            communication.containerTitle,
                            communication.title
                        )
                    } else {
                        context.getString(
                            R.string.conversation_title_conversation,
                            communication.courseTitle,
                            communication.containerTitle
                        )
                    }
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
        (type.ordinal shl 16) + (parentId % Int.MAX_VALUE).toInt(),
        Intent(context, DeleteNotificationReceiver::class.java)
            .putExtra(DeleteNotificationReceiver.PARENT_ID, parentId)
            .putExtra(DeleteNotificationReceiver.COMMUNICATION_TYPE, type.name),
        PendingIntent.FLAG_IMMUTABLE
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
            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
            .addRemoteInput(remoteInput)
            .build()
    }

    private fun buildMarkAsReadAction(
        context: Context,
        communication: PushCommunicationEntity
    ): NotificationCompat.Action {
        return NotificationCompat.Action.Builder(
            R.drawable.baseline_mark_chat_read_24,
            context.getString(R.string.push_notification_action_mark_as_read),
            constructDeletionIntent(context, communication.parentId, communication.type)
        )
            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_MARK_AS_READ)
            .build()
    }
}