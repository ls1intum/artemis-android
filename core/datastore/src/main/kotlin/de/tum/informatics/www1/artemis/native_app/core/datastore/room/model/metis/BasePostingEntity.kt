package de.tum.informatics.www1.artemis.native_app.core.datastore.room.model.metis

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import kotlinx.datetime.Instant

@Entity(
    tableName = "postings",
    primaryKeys = ["id"],
    foreignKeys = [
        ForeignKey(
            entity = MetisUserEntity::class,
            parentColumns = ["server_id", "id"],
            childColumns = ["server_id", "author_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ]
)
data class BasePostingEntity(
    @ColumnInfo(name = "id")
    val postId: String,
    @ColumnInfo(name = "server_id")
    val serverId: String,
    @ColumnInfo(name = "type")
    val postingType: PostingType,
    @ColumnInfo(name = "author_id")
    val authorId: Long,
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
        @ColumnInfo(name = "standalone")
        STANDALONE,
        @ColumnInfo(name = "answer")
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