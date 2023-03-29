package de.tum.informatics.www1.artemis.native_app.feature.push.notification_model

import de.tum.informatics.www1.artemis.native_app.feature.push.communication_notification_model.CommunicationType
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager.NotificationTargetManager
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ArtemisNotification<T : NotificationType>(
    val type: T,
    val notificationPlaceholders: List<String>,
    val target: String,
    val date: Instant
)

val ArtemisNotification<CommunicationNotificationType>.parentId: Long
    get() = NotificationTargetManager.getCommunicationNotificationTarget(
        type,
        target
    ).postId

val ArtemisNotification<CommunicationNotificationType>.communicationType: CommunicationType
    get() = when (type) {
        is StandalonePostCommunicationNotificationType, is ReplyPostCommunicationNotificationType -> CommunicationType.QUESTION_AND_ANSWER
    }