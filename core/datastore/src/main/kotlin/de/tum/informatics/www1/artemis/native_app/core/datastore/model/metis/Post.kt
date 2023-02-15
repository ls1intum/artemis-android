package de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis

import androidx.room.ColumnInfo
import androidx.room.Ignore
import androidx.room.Relation
import de.tum.informatics.www1.artemis.native_app.core.datastore.room.model.metis.*
import de.tum.informatics.www1.artemis.native_app.core.model.metis.IReaction
import de.tum.informatics.www1.artemis.native_app.core.model.metis.IStandalonePost
import de.tum.informatics.www1.artemis.native_app.core.model.metis.UserRole
import kotlinx.datetime.Instant

data class Post(
    @ColumnInfo(name = "client_post_id")
    val clientPostId: String,
    @ColumnInfo(name = "server_post_id")
    override val serverPostId: Long,
    @ColumnInfo(name = "title")
    override val title: String?,
    @ColumnInfo(name = "content")
    override val content: String,
    @ColumnInfo(name = "author_name")
    override val authorName: String,
    @ColumnInfo(name = "author_role")
    override val authorRole: UserRole,
    @ColumnInfo(name = "creation_date")
    override val creationDate: Instant,
    @ColumnInfo(name = "resolved")
    override val resolved: Boolean,
    @ColumnInfo(name = "context")
    val courseWideContext: BasePostingEntity.CourseWideContext?,
    @Relation(
        entity = StandalonePostTagEntity::class,
        entityColumn = "post_id",
        parentColumn = "client_post_id",
        projection = ["tag"]
    )
    override val tags: List<String>,
    @Relation(
        entity = AnswerPostingEntity::class,
        entityColumn = "parent_post_id",
        parentColumn = "client_post_id"
    )
    override val answers: List<AnswerPost>,
    @Relation(
        entity = PostReactionEntity::class,
        entityColumn = "post_id",
        parentColumn = "client_post_id",
        projection = ["author_id", "emoji", "id"]
    )
    override val reactions: List<Reaction>
) : IStandalonePost {

    @Ignore
    val orderedAnswerPostings = answers.sortedBy { it.creationDate }

    data class Reaction(
        @ColumnInfo(name = "emoji")
        override val emojiId: String,
        @ColumnInfo(name = "author_id")
        val authorId: Long,
        @Relation(
            entity = MetisUserEntity::class,
            parentColumn = "author_id",
            entityColumn = "id",
            projection = ["name"]
        )
        val username: String,
        @ColumnInfo(name = "id")
        val id: Long,
        @ColumnInfo(name = "creation_date")
        override val creationDate: Instant?
    ) : IReaction {
        @Ignore
        override val creatorId: Long = authorId
    }
}