package de.tum.informatics.www1.artemis.native_app.feature.push

import android.content.Context
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.MiscNotificationType

object ArtemisNotificationBuilder {

    fun generateNotificationContent(
        context: Context,
        notificationType: MiscNotificationType,
        placeholderValues: List<String>
    ): NotificationContent {
        return NotificationContent(context.getString(notificationType.title), context.getString(notificationType.body, *placeholderValues.toTypedArray()))
    }

    data class NotificationContent(val title: String, val body: String)
}