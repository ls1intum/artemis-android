package de.tum.informatics.www1.artemis.native_app.feature.push.communication_notification_model

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * A communication grouping for push notifications.
 * The [parentId] and [type] build the primary key.
 * [parentId] may have multiple meanings based on the [type]:
 * - [CommunicationType.QNA_COURSE], [CommunicationType.QNA_LECTURE], [CommunicationType.QNA_EXERCISE]: The id of the standalone post
 */
@Entity(
    tableName = "push_communication",
    primaryKeys = ["parent_id", "type"]
)
data class PushCommunicationEntity(
    @ColumnInfo(name = "parent_id")
    val parentId: Long,
    @ColumnInfo(name = "type")
    val type: CommunicationType,
    @ColumnInfo(name = "notification_id")
    val notificationId: Int,
    @ColumnInfo(name = "course_title")
    val courseTitle: String,
    /**
     * The container for this communication, for example the exercise name or the group chat name.
     */
    @ColumnInfo(name = "container_title")
    val containerTitle: String?,
    @ColumnInfo(name = "title")
    val title: String?,
    @ColumnInfo(name = "target")
    val target: String
)
