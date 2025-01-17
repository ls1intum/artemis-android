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
import de.tum.informatics.www1.artemis.native_app.core.common.ArtemisNotificationChannel
import de.tum.informatics.www1.artemis.native_app.core.common.markdown.PushNotificationArtemisMarkdownTransformer
import de.tum.informatics.www1.artemis.native_app.core.datastore.ArtemisNotificationManager
import de.tum.informatics.www1.artemis.native_app.feature.push.PushCommunicationDatabaseProvider
import de.tum.informatics.www1.artemis.native_app.feature.push.R
import de.tum.informatics.www1.artemis.native_app.feature.push.communication_notification_model.CommunicationMessageEntity
import de.tum.informatics.www1.artemis.native_app.feature.push.communication_notification_model.PushCommunicationEntity
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.ArtemisNotification
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.CommunicationNotificationType
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

            val communication = dbProvider.pushCommunicationDao.getCommunication(parentId)

            val messages = dbProvider.pushCommunicationDao.getCommunicationMessages(
                parentId
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
        authorName: String,
        body: String,
        date: Instant
    ) {
        dbProvider.pushCommunicationDao.insertSelfMessage(parentId, authorName, body, date)
        repopNotification(parentId)
    }

    override suspend fun repopNotification(
        parentId: Long
    ) {
        val (communication, messages) = dbProvider.database.withTransaction {
            if (!dbProvider.pushCommunicationDao.hasPushCommunication(parentId)
            ) return@withTransaction null to null

            val communication = dbProvider.pushCommunicationDao.getCommunication(parentId)

            val messages = dbProvider.pushCommunicationDao.getCommunicationMessages(parentId)

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
        val notificationChannel: ArtemisNotificationChannel =
            ArtemisNotificationChannel.CommunicationNotificationChannel

        val metisTarget =
            NotificationTargetManager.getCommunicationNotificationTarget(communication.target)

        val notification = NotificationCompat.Builder(context, notificationChannel.id)
            .setStyle(buildMessagingStyle(communication, messages))
            .setSmallIcon(R.drawable.baseline_chat_bubble_24)
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
                    parentId = communication.parentId
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


    override suspend fun deleteCommunication(parentId: Long) {
        val notificationId = dbProvider.database.withTransaction {
            if (!dbProvider.pushCommunicationDao.hasPushCommunication(parentId)
            ) return@withTransaction null

            val communication = dbProvider.pushCommunicationDao.getCommunication(parentId)
            dbProvider.pushCommunicationDao.deleteCommunication(parentId)
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
        val firstMessage = messages.first()
        val person = Person.Builder().setName(firstMessage.authorName).build()

        val style = NotificationCompat.MessagingStyle(person)
        messages.forEach { message ->
            style.addMessage(
                NotificationCompat.MessagingStyle.Message(
                    PushNotificationArtemisMarkdownTransformer.transformMarkdown(message.text),
                    message.date.toEpochMilliseconds(),
                    Person.Builder()
                        .setName(message.authorName)
                        .build()
                )
            )
        }

        style.isGroupConversation = true
        style.conversationTitle = if (communication.title != null) {
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

        return style
    }

    private fun constructDeletionIntent(
        context: Context,
        parentId: Long
    ): PendingIntent = PendingIntent.getBroadcast(
        context,
        (parentId % Int.MAX_VALUE).toInt(),
        Intent(context, DeleteNotificationReceiver::class.java)
            .putExtra(DeleteNotificationReceiver.PARENT_ID, parentId),
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
            constructDeletionIntent(context, communication.parentId)
        )
            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_MARK_AS_READ)
            .build()
    }
}
