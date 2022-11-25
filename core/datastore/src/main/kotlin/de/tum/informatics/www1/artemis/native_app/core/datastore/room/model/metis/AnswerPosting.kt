package de.tum.informatics.www1.artemis.native_app.core.datastore.room.model.metis

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "answer_postings",
    primaryKeys = ["server_id", "post_id", "course_id", "exercise_id", "lecture_id"],
    foreignKeys = [
        //The foreign key for its own entry in the posting table.
        ForeignKey(
            entity = PostingEntity::class,
            parentColumns = ["server_id", "id", "course_id", "exercise_id", "lecture_id"],
            childColumns = ["server_id", "post_id", "course_id", "exercise_id", "lecture_id"],
            onDelete = ForeignKey.CASCADE
        ),
        //The foreign key to the post that this post is an answer to
        ForeignKey(
            entity = PostingEntity::class,
            parentColumns = ["server_id", "id", "course_id", "exercise_id", "lecture_id"],
            childColumns = ["server_id", "parent_post_id", "course_id", "exercise_id", "lecture_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AnswerPosting(
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
    @ColumnInfo(name = "parent_post_id")
    val parentPostId: Int,
    @ColumnInfo(name = "resolves_post")
    val resolvesPost: Boolean
)