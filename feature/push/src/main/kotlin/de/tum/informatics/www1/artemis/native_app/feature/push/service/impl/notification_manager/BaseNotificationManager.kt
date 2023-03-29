package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager

import android.app.Notification
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationManagerCompat

interface BaseNotificationManager {

    fun popNotification(context: Context, notification: Notification, notificationId: Int) {
        try {
            NotificationManagerCompat
                .from(context)
                .notify(notificationId, notification)
        } catch (e: SecurityException) {
            Log.e("BaseNotificationManager", "Could not push notification due to missing permission.")
        }
    }
}