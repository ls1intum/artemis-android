package de.tum.informatics.www1.artemis.native_app.core.common

import android.app.NotificationManager

enum class ArtemisNotificationChannel(
    val id: String,
    val title: Int,
    val description: Int,
    val importance: Int = NotificationManager.IMPORTANCE_DEFAULT
) {
    MiscNotificationChannel(
        "misc-notification-channel",
        R.string.push_notification_channel_misc_name,
        R.string.push_notification_channel_misc_description,
        NotificationManager.IMPORTANCE_DEFAULT
    ),
    CommunicationNotificationChannel(
        "communication-notification-channel",
        R.string.push_notification_channel_communication_name,
        R.string.push_notification_channel_communication_description,
        NotificationManager.IMPORTANCE_HIGH
    )
}