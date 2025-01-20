package de.tum.informatics.www1.artemis.native_app.feature.push.communication_notification_model

import androidx.room.ColumnInfo
import androidx.room.Entity

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
    @ColumnInfo(name = "course_title")
    val courseTitle: String,
    /**
     * The container for this communication, for example the exercise name or the group chat name.
     */
    @ColumnInfo(name = "container_title")
    val containerTitle: String?,
    @ColumnInfo(name = "target")
    val target: String
)
