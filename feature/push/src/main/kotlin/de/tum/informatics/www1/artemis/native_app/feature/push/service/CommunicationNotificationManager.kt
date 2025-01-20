package de.tum.informatics.www1.artemis.native_app.feature.push.service

import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.ArtemisNotification
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.CommunicationNotificationType
import kotlinx.datetime.Instant

interface CommunicationNotificationManager {
    suspend fun popNotification(
        artemisNotification: ArtemisNotification<CommunicationNotificationType>
    )

    /**
     * Add a message to the notification sent by the user themself using direct reply.
     */
    suspend fun addSelfMessage(
        parentId: Long,
        authorName: String,
        body: String,
        date: Instant
    )

    /**
     * Reads the notification from the database and pops the notification
     */
    suspend fun repopNotification(
        parentId: Long
    )

    /**
     * Deletes a communication and clears the notification from the notification tray.
     */
    suspend fun deleteCommunication(parentId: Long)
}
