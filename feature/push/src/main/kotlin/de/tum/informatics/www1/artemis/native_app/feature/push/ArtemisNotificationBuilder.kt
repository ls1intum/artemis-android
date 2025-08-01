package de.tum.informatics.www1.artemis.native_app.feature.push

import android.content.Context
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.GeneralNotificationType

object ArtemisNotificationBuilder {

    fun generateNotificationContent(
        context: Context,
        notificationType: GeneralNotificationType,
        placeholderValues: List<String>
    ): NotificationContent {
        return NotificationContent(
            context.getString(notificationType.title, *placeholderValues.toTypedArray()),
            context.getString(notificationType.body, *placeholderValues.toTypedArray())
        )
    }

    data class NotificationContent(val title: String, val body: String)
}