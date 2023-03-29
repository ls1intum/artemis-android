package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager

import android.content.Context
import de.tum.informatics.www1.artemis.native_app.feature.push.ArtemisNotificationManager
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

    override fun popNotification(
        context: Context,
        notificationType: NotificationType,
        artemisNotification: ArtemisNotification
    ) {
        when (notificationType) {
            is MiscNotificationType -> {
                miscNotificationManager.popMiscNotification(
                    notificationType = notificationType,
                    artemisNotification = artemisNotification
                )
            }
            is CommunicationNotificationType -> {
                communicationNotificationManager.popNotification(
                    notificationType = notificationType,
                    artemisNotification = artemisNotification
                )
            }
        }
    }
}