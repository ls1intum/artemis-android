package de.tum.informatics.www1.artemis.native_app.feature.push.communication_notification_model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

@Entity(
    tableName = "push_communication_message",
    foreignKeys = [
        ForeignKey(
            entity = PushCommunicationEntity::class,
            parentColumns = ["parent_id"],
            childColumns = ["communication_parent_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(
            "communication_parent_id",
            name = "i_communication_parent_id_communication_type"
        )
    ]
)
data class CommunicationMessageEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "communication_parent_id")
    val communicationParentId: Long,
    @ColumnInfo(name = "text")
    val text: String,
    @ColumnInfo(name = "author_id")
    val authorId: String,
    @ColumnInfo(name = "author_name")
    val authorName: String,
    @ColumnInfo(name = "author_image_url")
    val authorImageUrl: String?,
    @ColumnInfo(name = "date")
    val date: Instant
)