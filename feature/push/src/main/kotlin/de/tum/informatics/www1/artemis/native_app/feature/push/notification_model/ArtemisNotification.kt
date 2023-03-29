package de.tum.informatics.www1.artemis.native_app.feature.push.notification_model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ArtemisNotification(
    val type: NotificationType,
    val notificationPlaceholders: List<String>,
    val target: String,
    val date: Instant
)