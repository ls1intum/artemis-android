package de.tum.informatics.www1.artemis.native_app.core.datastore.room.model.metis

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "standalone_postings",
    primaryKeys = ["server_id", "post_id", "course_id", "exercise_id", "lecture_id"],
    foreignKeys = [
        ForeignKey(
            entity = PostingEntity::class,
            parentColumns = ["server_id", "id", "course_id", "exercise_id", "lecture_id"],
            childColumns = ["server_id", "post_id", "course_id", "exercise_id", "lecture_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
class StandalonePosting(
    @ColumnInfo(name = "server_id")
    val serverId: String,
    @ColumnInfo(name = "post_id")
    val postId: Int,
    @ColumnInfo(name = "course_id")
    val courseId: Int,
    @ColumnInfo(name = "exercise_id")
    val exerciseId: Int,
    @ColumnInfo(name = "lecture_id")
    val lectureId: Int,
    @ColumnInfo(name = "title")
    val title: String?,
    @ColumnInfo(name = "context")
    val context: PostingEntity.CourseWideContext?,
    @ColumnInfo(name = "display_priority")
    val displayPriority: PostingEntity.DisplayPriority?,
    @ColumnInfo(name = "resolved")
    val resolved: Boolean
)