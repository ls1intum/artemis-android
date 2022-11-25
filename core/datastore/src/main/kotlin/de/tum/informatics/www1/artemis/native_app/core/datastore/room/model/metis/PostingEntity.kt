package de.tum.informatics.www1.artemis.native_app.core.datastore.room.model.metis

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import kotlinx.datetime.Instant

@Entity(
    tableName = "postings",
    primaryKeys = ["id", "server_id", "course_id", "lecture_id", "exercise_id"],
    foreignKeys = [
        ForeignKey(
            entity = MetisUserEntity::class,
            parentColumns = ["server_id", "id"],
            childColumns = ["server_id", "author_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ]
)
data class PostingEntity(
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "server_id")
    val serverId: String,
    @ColumnInfo(name = "course_id")
    val courseId: Int,
    @ColumnInfo(name = "exercise_id")
    val exerciseId: Int,
    @ColumnInfo(name = "lecture_id")
    val lectureId: Int,
    @ColumnInfo(name = "type")
    val postingType: PostingType,
    @ColumnInfo(name = "author_id")
    val authorId: Int,
    @ColumnInfo(name = "creation_date")
    val creationDate: Instant,
    @ColumnInfo(name = "content")
    val content: String?,
    @ColumnInfo(name = "author_role")
    val authorRole: UserRole
) {
    enum class CourseWideContext {
        @ColumnInfo(name = "tech_support")
        TECH_SUPPORT,

        @ColumnInfo(name = "organization")
        ORGANIZATION,

        @ColumnInfo(name = "random")
        RANDOM,

        @ColumnInfo(name = "announcement")
        ANNOUNCEMENT
    }

    enum class DisplayPriority {
        @ColumnInfo(name = "pinned")
        PINNED,

        @ColumnInfo(name = "archived")
        ARCHIVED,

        @ColumnInfo(name = "none")
        NONE
    }

    enum class PostingType {
        STANDALONE,
        ANSWER
    }

    enum class UserRole {
        @ColumnInfo(name = "instructor")
        INSTRUCTOR,

        @ColumnInfo(name = "tutor")
        TUTOR,

        @ColumnInfo(name = "user")
        USER
    }
}