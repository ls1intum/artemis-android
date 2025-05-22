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
import de.tum.informatics.www1.artemis.native_app.core.common.markdown.ArtemisMarkdownTransformer
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.AccountDataService
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
import de.tum.informatics.www1.artemis.native_app.feature.push.service.CommunicationNotificationManager
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager.delete.DeleteNotificationReceiver
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager.mark_as_read.MarkAsReadReceiver
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager.mute.MuteConversationReceiver
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager.reply.ReplyReceiver
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager.util.NotificationTargetManager
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager.util.toCircleShape
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
    private val accountDataService: AccountDataService,
) : CommunicationNotificationManager, BaseNotificationManager {

    val mutableFlags: Int
        get() = if (Build.VERSION.SDK_INT >= 31) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else PendingIntent.FLAG_UPDATE_CURRENT

    override suspend fun popNotification(
        artemisNotification: ArtemisNotification<CommunicationNotificationType>
    ) {
        var isPostFromAppUser = false
        val (communication, messages) = dbProvider.database.withTransaction {
            val parentId = dbProvider.pushCommunicationDao.insertNotification(
                artemisNotification = artemisNotification,
                generateNotificationId = {
                    ArtemisNotificationManager.getNextNotificationId(context)
                },
                isPostFromAppUser = { content ->
                    isPostFromAppUser = content.authorId == getClientId().toString()
                    isPostFromAppUser
                }
            )

            val communication = dbProvider.pushCommunicationDao.getCommunication(parentId)

            val messages = dbProvider.pushCommunicationDao.getCommunicationMessages(
                parentId
            )

            communication to messages
        }

        // Without this check, we would pop previous notifications in the following scenario:
        // 1. App user creates post1.
        // 2. Other user replies answer1 in post1's thread.
        //      -> User gets notification for answer1 (desired).
        // 3. App user replies answer2 in post1's thread.
        //      -> User would get not get notification for answer2 (desired).
        //      -> BUT: Notification for answer1 would be popped again (not desired).
        if (isPostFromAppUser) return

        if (messages.isEmpty()) return
        popCommunicationNotification(communication, messages)
    }

    private suspend fun getClientId(): Long? {
        val accountData = accountDataService.getCachedAccountData()
        return accountData?.id
    }

    override suspend fun addSelfMessage(
        parentId: Long,
        authorLoginName: String,
        authorName: String,
        authorImageUrl: String?,
        body: String,
        date: Instant
    ) {
        dbProvider.pushCommunicationDao.insertSelfMessage(
            parentId = parentId,
            authorLoginName = authorLoginName,
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

        val notification = NotificationCompat.Builder(context, notificationChannel.id)
            .setStyle(buildMessagingStyle(communication, messages))
            .setSmallIcon(R.drawable.push_notification_icon)
            .setAutoCancel(true)
            .setContentIntent(
                NotificationTargetManager.getMetisContentIntent(
                    context = context,
                    notificationTarget = communication.target
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
            .addAction(
                buildMarkAsReadAction(
                    context = context,
                    communication = communication,
                )
            )
            .addAction(
                buildMuteAction(
                    context = context,
                    communication = communication,
                )
            )
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
                    ArtemisMarkdownTransformer.Default.transformMarkdown(message.text),
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
                return IconCompat.createWithBitmap(bitmap.toCircleShape())
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
        parentId.asRequestCode(),
        buildIntentWithParentId<DeleteNotificationReceiver>(parentId),
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

        val resultIntent = buildIntentWithParentId<ReplyReceiver>(communication.parentId)
        val pendingIntent: PendingIntent =
            PendingIntent.getBroadcast(
                context,
                notificationId, // Set request code to notification id.
                resultIntent,
                mutableFlags
            )

        return NotificationCompat.Action.Builder(
            R.drawable.reply,
            replyText,
            pendingIntent
        )
            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
            .addRemoteInput(remoteInput)
            .build()
    }

    private fun buildMarkAsReadAction(
        context: Context,
        communication: PushCommunicationEntity,
    ): NotificationCompat.Action {
        val parentId = communication.parentId
        val resultIntent = buildIntentWithParentId<MarkAsReadReceiver>(parentId)
        val pendingIntent: PendingIntent =
            PendingIntent.getBroadcast(
                context,
                parentId.asRequestCode(),
                resultIntent,
                mutableFlags
            )

        return NotificationCompat.Action.Builder(
            R.drawable.baseline_mark_chat_read_24,
            context.getString(R.string.push_notification_action_mark_as_read),
            pendingIntent
        )
            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_MARK_AS_READ)
            .build()
    }

    private fun buildMuteAction(
        context: Context,
        communication: PushCommunicationEntity,
    ): NotificationCompat.Action {
        val parentId = communication.parentId
        val resultIntent = buildIntentWithParentId<MuteConversationReceiver>(parentId)
        val pendingIntent: PendingIntent =
            PendingIntent.getBroadcast(
                context,
                parentId.asRequestCode(),
                resultIntent,
                mutableFlags
            )

        return NotificationCompat.Action.Builder(
            R.drawable.mute,
            context.getString(R.string.push_notification_action_mute),
            pendingIntent
        )
            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_MUTE)
            .build()
    }

    private inline fun <reified T: BaseCommunicationNotificationReceiver>buildIntentWithParentId(
        parentId: Long
    ) = Intent(context, T::class.java)
        .putExtra(BaseCommunicationNotificationReceiver.PARENT_ID, parentId)

    private fun Long.asRequestCode() = (this % Int.MAX_VALUE).toInt()
}

