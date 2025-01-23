package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.UserRole
import kotlinx.datetime.Instant

@Entity(
    tableName = "postings",
    primaryKeys = ["id"],
    foreignKeys = [
        ForeignKey(
            entity = MetisUserEntity::class,
            parentColumns = ["server_id", "id"],
            childColumns = ["server_id", "author_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("server_id", "author_id", name = "sa_index")]
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
    @ColumnInfo(name = "updated_date")
    val updatedDate: Instant?,
    @ColumnInfo(name = "content")
    val content: String?,
    @ColumnInfo(name = "author_role")
    val authorRole: UserRole?,
    @ColumnInfo(name = "is_saved")
    val isSaved: Boolean?,
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
}