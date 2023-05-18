package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager

import android.content.Context
import androidx.core.app.NotificationCompat
import de.tum.informatics.www1.artemis.native_app.feature.push.ArtemisNotificationChannel
import de.tum.informatics.www1.artemis.native_app.feature.push.ArtemisNotificationManager
import de.tum.informatics.www1.artemis.native_app.feature.push.R
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.ArtemisNotification
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.CommunicationArtemisNotification
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.CommunicationNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.MiscArtemisNotification
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.UnknownArtemisNotification
import de.tum.informatics.www1.artemis.native_app.feature.push.service.CommunicationNotificationManager
import de.tum.informatics.www1.artemis.native_app.feature.push.service.NotificationManager

/**
 * Notification manager that delegates handling the actual push notifications to [MiscNotificationManager] and [CommunicationNotificationManager]
 */
internal class NotificationManagerImpl(
    private val miscNotificationManager: MiscNotificationManager,
    private val communicationNotificationManager: CommunicationNotificationManager
) : NotificationManager, BaseNotificationManager {

    @Suppress("UNCHECKED_CAST")
    override suspend fun popNotification(
        context: Context,
        artemisNotification: ArtemisNotification<*>
    ) {
        when (artemisNotification) {
            is MiscArtemisNotification -> {
                miscNotificationManager.popMiscNotification(
                    artemisNotification = artemisNotification
                )
            }

            is CommunicationArtemisNotification -> {
                communicationNotificationManager.popNotification(
                    artemisNotification = artemisNotification as ArtemisNotification<CommunicationNotificationType>
                )
            }

            is UnknownArtemisNotification -> {
                // pop unknown notification
                popNotification(
                    context,
                    NotificationCompat.Builder(
                        context,
                        ArtemisNotificationChannel.MiscNotificationChannel.id
                    )
                        .setContentTitle(context.getString(R.string.push_notification_unknown_title))
                        .setContentText(context.getString(R.string.push_notification_unknown_body))
                        .setSmallIcon(R.drawable.push_notification_icon)
                        .build(),
                    ArtemisNotificationManager.getNextNotificationId(context)
                )
            }
        }
    }
}