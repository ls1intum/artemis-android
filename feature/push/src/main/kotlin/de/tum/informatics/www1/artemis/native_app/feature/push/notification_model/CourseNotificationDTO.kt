package de.tum.informatics.www1.artemis.native_app.feature.push.notification_model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CourseNotificationDTO(
    @SerialName("notificationType")
    @Serializable(with = NotificationTypeSerializer::class)
    val notificationType: NotificationType,
    @SerialName("notificationId")
    val notificationId: Long,
    @SerialName("courseId")
    val courseId: Long,
    @SerialName("creationDate")
    @Serializable(with = SafeInstantSerializer::class)
    val creationDate: Instant,
    @SerialName("category")
    val category: NotificationCategory,
    @SerialName("parameters")
    val parameters: CourseNotificationParameters,
    @SerialName("status")
    val status: NotificationStatus
)

@Serializable
data class CourseNotificationParameters(
    @SerialName("courseTitle")
    val courseTitle: String? = null,
    @SerialName("authorName")
    val authorName: String? = null,
    @SerialName("postMarkdownContent")
    val postMarkdownContent: String? = null,
    @SerialName("channelName")
    val channelName: String? = null,
    @SerialName("channelType")
    val channelType: String? = null,
    @SerialName("postId")
    val postId: Long? = null,
    @SerialName("authorId")
    val authorId: Long? = null,
    @SerialName("courseIconUrl")
    val courseIconUrl: String? = null,
    @SerialName("authorImageUrl")
    val authorImageUrl: String? = null,
    @SerialName("channelId")
    val channelId: Long? = null,
    // Additional parameters for other notification types
    @SerialName("exerciseId")
    val exerciseId: Long? = null,
    @SerialName("exerciseTitle")
    val exerciseTitle: String? = null,
    @SerialName("exerciseType")
    val exerciseType: String? = null,
    @SerialName("numberOfPoints")
    val numberOfPoints: Int? = null,
    @SerialName("score")
    val score: Int? = null,
    @SerialName("difficulty")
    val difficulty: String? = null,
    @SerialName("releaseDate")
    val releaseDate: String? = null,
    @SerialName("dueDate")
    val dueDate: String? = null,
    @SerialName("groupTitle")
    val groupTitle: String? = null,
    @SerialName("groupId")
    val groupId: Long? = null,
    @SerialName("moderatorName")
    val moderatorName: String? = null,
    @SerialName("attachmentName")
    val attachmentName: String? = null,
    @SerialName("exerciseOrLectureName")
    val exerciseOrLectureName: String? = null,
    @SerialName("lectureId")
    val lectureId: Long? = null,
    @SerialName("verdict")
    val verdict: String? = null,
    @SerialName("postTitle")
    val postTitle: String? = null,
    @SerialName("postCreationDate")
    val postCreationDate: String? = null,
    @SerialName("postAuthorName")
    val postAuthorName: String? = null,
    @SerialName("replyMarkdownContent")
    val replyMarkdownContent: String? = null,
    @SerialName("replyCreationDate")
    val replyCreationDate: String? = null,
    @SerialName("replyAuthorName")
    val replyAuthorName: String? = null,
    @SerialName("replyAuthorId")
    val replyAuthorId: Long? = null,
    @SerialName("replyImageUrl")
    val replyImageUrl: String? = null,
    @SerialName("replyId")
    val replyId: Long? = null,
    @SerialName("channelModerator")
    val channelModerator: String? = null,
    @SerialName("deletingUser")
    val deletingUser: String? = null
)

@Serializable
enum class NotificationCategory {
    @SerialName("COMMUNICATION")
    COMMUNICATION,
    @SerialName("GENERAL")
    GENERAL,
    @SerialName("UNKNOWN")
    UNKNOWN
}

enum class NotificationFilter {
    COMMUNICATION,
    GENERAL,
}

@Serializable
enum class NotificationStatus {
    @SerialName("UNSEEN")
    UNSEEN,
    @SerialName("SEEN")
    SEEN,
    @SerialName("UNKNOWN")
    UNKNOWN
}
