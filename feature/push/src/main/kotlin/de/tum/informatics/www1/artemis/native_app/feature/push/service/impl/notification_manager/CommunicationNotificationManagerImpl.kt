package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.graphics.drawable.IconCompat
import androidx.room.withTransaction
import coil3.request.ErrorResult
import coil3.request.SuccessResult
import coil3.toBitmap
import de.tum.informatics.www1.artemis.native_app.core.common.ArtemisNotificationChannel
import de.tum.informatics.www1.artemis.native_app.core.common.markdown.PushNotificationArtemisMarkdownTransformer
import de.tum.informatics.www1.artemis.native_app.core.datastore.ArtemisNotificationManager
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.ArtemisImageProvider
import de.tum.informatics.www1.artemis.native_app.feature.push.PushCommunicationDatabaseProvider
import de.tum.informatics.www1.artemis.native_app.feature.push.R
import de.tum.informatics.www1.artemis.native_app.feature.push.communication_notification_model.CommunicationMessageEntity
import de.tum.informatics.www1.artemis.native_app.feature.push.communication_notification_model.CommunicationNotificationConversationType
import de.tum.informatics.www1.artemis.native_app.feature.push.communication_notification_model.PushCommunicationEntity
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.ArtemisNotification
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.CommunicationNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.ReplyPostCommunicationNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.parentId
import de.tum.informatics.www1.artemis.native_app.feature.push.service.CommunicationNotificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant

private const val TAG = "CommunicationNotificationManagerImpl"

/**
 * Handles communication notifications and pops them as communication style push notifications.
 * Uses a database to store previously sent notification.
 */
internal class CommunicationNotificationManagerImpl(
    private val context: Context,
    private val dbProvider: PushCommunicationDatabaseProvider,
    private val artemisImageProvider: ArtemisImageProvider,
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

    override suspend fun addSelfMessage(
        parentId: Long,
        authorName: String,
        authorImageUrl: String?,
        body: String,
        date: Instant
    ) {
        dbProvider.pushCommunicationDao.insertSelfMessage(
            parentId = parentId,
            authorName = authorName,
            authorImageUrl = authorImageUrl,
            body = body,
            date = date
        )
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
            .setSmallIcon(R.drawable.push_notification_icon)
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
    ): NotificationCompat.MessagingStyle = runBlocking {
        val firstMessage = messages.first()
        val person = Person.Builder().setName(firstMessage.authorName).build()

        val style = NotificationCompat.MessagingStyle(person)
        messages.forEach { message ->
            val icon = withContext(Dispatchers.IO) {
                getMessageIcon(message)
            }

            style.addMessage(
                NotificationCompat.MessagingStyle.Message(
                    PushNotificationArtemisMarkdownTransformer.transformMarkdown(message.text),
                    message.date.toEpochMilliseconds(),
                    Person.Builder()
                        .setIcon(icon)
                        .setName(message.authorName)
                        .build()
                )
            )
        }

        style.isGroupConversation = when (communication.conversationType) {
            CommunicationNotificationConversationType.ONE_TO_ONE_CHAT -> false
            else -> true
        }

        val isThread = communication.notificationType is ReplyPostCommunicationNotificationType
        style.conversationTitle = if (isThread) {
            context.getString(
                R.string.conversation_title_conversation_thread,
                communication.courseTitle,
                communication.containerTitle
            )
        } else {
            context.getString(
                R.string.conversation_title_conversation,
                communication.courseTitle,
                communication.containerTitle
            )
        }

        return@runBlocking style
    }

    private suspend fun getMessageIcon(message: CommunicationMessageEntity): IconCompat? {
        val result = artemisImageProvider.loadArtemisImage(
            context = context,
            imagePath = message.authorImageUrl ?: return null
        )

        when (result) {
            is SuccessResult -> {
                val bitmap = result.image.toBitmap()
                return IconCompat.createWithBitmap(bitmap)
            }

            is ErrorResult -> {
                Log.e(TAG, "Error while loading notification profile image: ${result.throwable}")
                return null
            }

        }
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
