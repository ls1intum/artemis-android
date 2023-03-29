package de.tum.informatics.www1.artemis.native_app.feature.push.communication_notification_model

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * A communication grouping for push notifications.
 * The [parentId] and [type] build the primary key.
 * [parentId] may have multiple meanings based on the [type]:
 * - [CommunicationType.QUESTION_AND_ANSWER]: The id of the standalone post
 */
@Entity(
    tableName = "push_communication",
    primaryKeys = ["parent_id", "type"]
)
data class PushCommunicationEntity(
    @ColumnInfo(name = "parent_id")
    val parentId: Long,
    @ColumnInfo(name = "type")
    val type: CommunicationType
)