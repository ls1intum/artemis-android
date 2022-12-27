package de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis

import androidx.room.ColumnInfo
import androidx.room.Ignore
import androidx.room.Relation
import de.tum.informatics.www1.artemis.native_app.core.datastore.room.model.metis.*
import kotlinx.datetime.Instant

data class Post(
    @ColumnInfo(name = "client_post_id")
    val clientPostId: String,
    @ColumnInfo(name = "server_post_id")
    val serverPostId: Long,
    @ColumnInfo(name = "title")
    val title: String?,
    @ColumnInfo(name = "content")
    val content: String,
    @ColumnInfo(name = "author_name")
    val authorName: String,
    @ColumnInfo(name = "author_role")
    val authorRole: BasePostingEntity.UserRole,
    @ColumnInfo(name = "creation_date")
    val creationDate: Instant,
    @ColumnInfo(name = "resolved")
    val resolved: Boolean,
    @ColumnInfo(name = "context")
    val courseWideContext: BasePostingEntity.CourseWideContext?,
    @Relation(
        entity = StandalonePostTagEntity::class,
        entityColumn = "post_id",
        parentColumn = "client_post_id",
        projection = ["tag"]
    )
    val tags: List<String>,
    @Relation(
        entity = AnswerPostingEntity::class,
        entityColumn = "parent_post_id",
        parentColumn = "client_post_id"
    )
    val answerPostings: List<AnswerPost>,
    @Relation(
        entity = PostReactionEntity::class,
        entityColumn = "post_id",
        parentColumn = "client_post_id",
        projection = ["author_id", "emoji", "id"]
    )
    val reactions: List<Reaction>
) {

    @Ignore
    val orderedAnswerPostings = answerPostings.sortedBy { it.creationDate }

    data class Reaction(
        @ColumnInfo(name = "emoji")
        val emojiId: String,
        @ColumnInfo(name = "author_id")
        val authorId: Int,
        @Relation(
            entity = MetisUserEntity::class,
            parentColumn = "author_id",
            entityColumn = "id",
            projection = ["name"]
        )
        val username: String,
        @ColumnInfo(name = "id")
        val id: Int
    )
}