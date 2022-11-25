package de.tum.informatics.www1.artemis.native_app.core.datastore.room.model.metis

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "post_tags",
    primaryKeys = ["server_id", "post_id", "course_id", "lecture_id", "exercise_id"],
    foreignKeys = [
        ForeignKey(
            entity = PostingEntity::class,
            parentColumns = ["server_id", "id", "course_id", "lecture_id", "exercise_id"],
            childColumns = ["server_id", "post_id", "course_id", "lecture_id", "exercise_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class StandalonePostTag(
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
    @ColumnInfo(name = "tag")
    val tag: String
)