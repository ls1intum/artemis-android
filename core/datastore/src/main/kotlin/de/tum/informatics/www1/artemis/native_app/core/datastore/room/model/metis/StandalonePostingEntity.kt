package de.tum.informatics.www1.artemis.native_app.core.datastore.room.model.metis

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "standalone_postings",
    primaryKeys = ["post_id"],
    foreignKeys = [
        ForeignKey(
            entity = BasePostingEntity::class,
            parentColumns = ["id"],
            childColumns = ["post_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
class StandalonePostingEntity(
    @ColumnInfo(name = "post_id")
    val postId: String,
    @ColumnInfo(name = "title")
    val title: String?,
    @ColumnInfo(name = "context")
    val context: BasePostingEntity.CourseWideContext?,
    @ColumnInfo(name = "display_priority")
    val displayPriority: BasePostingEntity.DisplayPriority?,
    @ColumnInfo(name = "resolved")
    val resolved: Boolean,
    @ColumnInfo(name = "live_created")
    val liveCreated: Boolean
)