package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager

import android.content.Context
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.ArtemisNotification
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.CommunicationNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.MiscNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.NotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.service.NotificationManager

/**
 * Notification manager that delegates handling the actual push notifications to [MiscNotificationManager] and [CommunicationNotificationManager]
 */
internal class NotificationManagerImpl(
    private val miscNotificationManager: MiscNotificationManager,
    private val communicationNotificationManager: CommunicationNotificationManager
) : NotificationManager {

    @Suppress("UNCHECKED_CAST")
    override suspend fun popNotification(
        context: Context,
        artemisNotification: ArtemisNotification<NotificationType>
    ) {
        when (artemisNotification.type) {
            is MiscNotificationType -> {
                miscNotificationManager.popMiscNotification(
                    artemisNotification = artemisNotification as ArtemisNotification<MiscNotificationType>
                )
            }
            is CommunicationNotificationType -> {
                communicationNotificationManager.popNotification(
                    artemisNotification = artemisNotification as ArtemisNotification<CommunicationNotificationType>
                )
            }
        }
    }
}