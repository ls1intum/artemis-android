package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.room.withTransaction
import de.tum.informatics.www1.artemis.native_app.feature.push.ArtemisNotificationManager
import de.tum.informatics.www1.artemis.native_app.feature.push.PushCommunicationDatabaseProvider
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
        val notification = NotificationCompat
    }

    private fun buildMessagingStyle(
        communication: PushCommunicationEntity,
        messages: List<CommunicationMessageEntity>
    ): NotificationCompat.MessagingStyle {
        val person = when (communication.type) {
            CommunicationType.QUESTION_AND_ANSWER -> {
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

        when(communication.type) {
            CommunicationType.QUESTION_AND_ANSWER -> {
                style.isGroupConversation = true
                style.conversationTitle = when()
            }
        }

        return style
    }
}