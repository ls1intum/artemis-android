package de.tum.informatics.www1.artemis.native_app.feature.push.communication_notification_model

import androidx.room.ColumnInfo
import androidx.room.Entity
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.CommunicationNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.ReplyPostCommunicationNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.StandalonePostCommunicationNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.target.CommunicationPostTarget
import kotlinx.serialization.json.Json

/**
 * A communication grouping for push notifications.
 * [parentId] is the id of the standalone post
 */
@Entity(
    tableName = "push_communication",
    primaryKeys = ["parent_id"]
)
data class PushCommunicationEntity(
    @ColumnInfo(name = "parent_id")
    val parentId: Long,
    @ColumnInfo(name = "notification_id")
    val notificationId: Int,
    @ColumnInfo(name = "notification_type")
    val notificationTypeString: String,
    @ColumnInfo(name = "course_title")
    val courseTitle: String,
    /**
     * The container for this communication, for example the exercise name or the group chat name.
     */
    @ColumnInfo(name = "container_title")
    val containerTitle: String?,
    @ColumnInfo(name = "target")
    val targetString: String,
    @ColumnInfo(name = "chat_type")
    val conversationTypeString: String?,
) {

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
        }
    }

    val conversationType: CommunicationNotificationConversationType?
        get() = conversationTypeString?.let { CommunicationNotificationConversationType.fromRawValue(it) }

    val notificationType: CommunicationNotificationType
        get() = try {
                StandalonePostCommunicationNotificationType.valueOf(notificationTypeString)
            } catch (e: IllegalArgumentException) {
                ReplyPostCommunicationNotificationType.valueOf(notificationTypeString)
            }

    val target: CommunicationPostTarget
        get() = try {
            json.decodeFromString<CommunicationPostTarget>(targetString)
        } catch (e: Exception) {
            // Fallback to a default target if parsing fails
            CommunicationPostTarget(
                message = "new-message",
                entity = "message",
                postId = parentId,
                courseId = 0L,
                conversationId = 0L
            )
        }
}
