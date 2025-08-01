package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager

import android.content.Context
import androidx.core.app.NotificationCompat
import de.tum.informatics.www1.artemis.native_app.core.common.ArtemisNotificationChannel
import de.tum.informatics.www1.artemis.native_app.core.datastore.ArtemisNotificationManager
import de.tum.informatics.www1.artemis.native_app.feature.push.ArtemisNotificationBuilder
import de.tum.informatics.www1.artemis.native_app.feature.push.R
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.ArtemisNotification
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.GeneralNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.getNotificationPlaceholders
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.isDisplayable
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager.util.NotificationTargetManager
import kotlinx.coroutines.runBlocking

internal class GeneralNotificationManager(private val context: Context) : BaseNotificationManager {

    /**
     * Pop a notification based on the decrypted payload received from the server.
     */
    fun popGeneralNotification(
        artemisNotification: ArtemisNotification<GeneralNotificationType>
    ) {
        // If the notification type is not displayable, we do not show it
        if ((artemisNotification.courseNotificationDTO.notificationType as? GeneralNotificationType)?.isDisplayable() == false) {
            return
        }

        val placeholders = artemisNotification.getNotificationPlaceholders()

        val notificationContent = ArtemisNotificationBuilder.generateNotificationContent(
            context,
            artemisNotification.courseNotificationDTO.notificationType as GeneralNotificationType,
            placeholders
        )

        val notification = NotificationCompat.Builder(context, ArtemisNotificationChannel.MiscNotificationChannel.id)
            .apply {
                setContentTitle(notificationContent.title)
                setContentText(notificationContent.body)

                setSmallIcon(R.drawable.push_notification_icon)
                setContentIntent(NotificationTargetManager.buildOnClickIntent(context, artemisNotification))
                setAutoCancel(true)
            }
            .build()

        val notificationId = runBlocking { ArtemisNotificationManager.getNextNotificationId(context) }

        popNotification(context, notification, notificationId)
    }
}