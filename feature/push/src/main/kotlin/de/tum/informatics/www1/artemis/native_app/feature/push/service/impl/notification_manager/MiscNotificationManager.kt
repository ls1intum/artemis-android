package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager

import android.content.Context
import androidx.core.app.NotificationCompat
import de.tum.informatics.www1.artemis.native_app.core.common.ArtemisNotificationChannel
import de.tum.informatics.www1.artemis.native_app.core.datastore.ArtemisNotificationManager
import de.tum.informatics.www1.artemis.native_app.feature.push.ArtemisNotificationBuilder
import de.tum.informatics.www1.artemis.native_app.feature.push.R
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.ArtemisNotification
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.MiscNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager.util.NotificationTargetManager
import kotlinx.coroutines.runBlocking

internal class MiscNotificationManager(private val context: Context) : BaseNotificationManager {

    /**
     * Pop a notification based on the decrypted payload received from the server.
     */
    fun popMiscNotification(
        artemisNotification: ArtemisNotification<MiscNotificationType>
    ) {
        val notificationContent = ArtemisNotificationBuilder.generateNotificationContent(
            context,
            artemisNotification.type,
            artemisNotification.notificationPlaceholders
        )

        val notification = NotificationCompat.Builder(context, ArtemisNotificationChannel.MiscNotificationChannel.id)
            .apply {
                setContentTitle(notificationContent.title)
                setContentText(notificationContent.body)

                setSmallIcon(R.drawable.push_notification_icon)
                setContentIntent(NotificationTargetManager.buildOnClickIntent(context, artemisNotification.type, artemisNotification.target))
                setAutoCancel(true)
            }
            .build()

        val notificationId = runBlocking { ArtemisNotificationManager.getNextNotificationId(context) }

        popNotification(context, notification, notificationId)
    }
}