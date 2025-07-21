package de.tum.informatics.www1.artemis.native_app.feature.push.notification_model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class CourseNotificationDTO(
    @Serializable(with = NotificationTypeSerializer::class)
    val notificationType: NotificationType,
    val notificationId: Long,
    val courseId: Long,
    @Serializable(with = SafeInstantSerializer::class)
    val creationDate: Instant,
    val category: NotificationCategory,
    val parameters: CourseNotificationParameters,
    val status: NotificationStatus
)

@Serializable
data class CourseNotificationParameters(
    val courseTitle: String? = null,
    val authorName: String? = null,
    val postMarkdownContent: String? = null,
    val channelName: String? = null,
    val channelType: String? = null,
    val postId: Long? = null,
    val authorId: Long? = null,
    val courseIconUrl: String? = null,
    val authorImageUrl: String? = null,
    val channelId: Long? = null,
    val exerciseId: Long? = null,
    val exerciseTitle: String? = null,
    val exerciseType: String? = null,
    val numberOfPoints: Int? = null,
    val score: Int? = null,
    val difficulty: String? = null,
    val releaseDate: String? = null,
    val dueDate: String? = null,
    val groupTitle: String? = null,
    val groupId: Long? = null,
    val moderatorName: String? = null,
    val attachmentName: String? = null,
    val exerciseOrLectureName: String? = null,
    val lectureId: Long? = null,
    val verdict: String? = null,
    val postTitle: String? = null,
    val postCreationDate: String? = null,
    val postAuthorName: String? = null,
    val replyMarkdownContent: String? = null,
    val replyCreationDate: String? = null,
    val replyAuthorName: String? = null,
    val replyAuthorId: Long? = null,
    val replyImageUrl: String? = null,
    val replyId: Long? = null,
    val channelModerator: String? = null,
    val deletingUser: String? = null
)

@Serializable
enum class NotificationCategory {
    COMMUNICATION,
    GENERAL,
    UNKNOWN
}

enum class NotificationFilter {
    COMMUNICATION,
    GENERAL,
}

@Serializable
enum class NotificationStatus {
    UNSEEN,
    SEEN,
    UNKNOWN
}
