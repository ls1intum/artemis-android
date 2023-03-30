package de.tum.informatics.www1.artemis.native_app.feature.metis.model

import androidx.room.ColumnInfo
import androidx.room.Ignore
import androidx.room.Relation
import de.tum.informatics.www1.artemis.native_app.core.model.metis.IAnswerPost
import de.tum.informatics.www1.artemis.native_app.core.model.metis.UserRole
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.room.BasePostingEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.room.MetisUserEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.room.PostReactionEntity
import kotlinx.datetime.Instant

data class AnswerPost constructor(
    @ColumnInfo(name = "parent_post_id")
    val parentPostId: String,
    @ColumnInfo(name = "post_id")
    val postId: String,
    @ColumnInfo(name = "resolves_post")
    override val resolvesPost: Boolean,
    @Relation(
        entity = BasePostingEntity::class,
        entityColumn = "id",
        parentColumn = "post_id",
        projection = ["author_id", "creation_date", "content", "author_role"]
    )
    private val basePostingCache: BasePostingCache,
    @Relation(
        entity = PostReactionEntity::class,
        entityColumn = "post_id",
        parentColumn = "post_id",
        projection = ["author_id", "emoji", "id"]
    )
    override val reactions: List<Post.Reaction>
) : IAnswerPost {
    @Ignore
    override val creationDate: Instant = basePostingCache.creationDate
    @Ignore
    override val content: String? = basePostingCache.content
    @Ignore
    override val authorRole: UserRole = basePostingCache.authorRole
    @Ignore
    override val authorName: String = basePostingCache.authorName

    data class BasePostingCache(
        @ColumnInfo(name = "author_id")
        val authorId: Long,
        @ColumnInfo(name = "creation_date")
        val creationDate: Instant,
        @ColumnInfo(name = "content")
        val content: String?,
        @ColumnInfo(name = "author_role")
        val authorRole: UserRole,
        @Relation(
            entity = MetisUserEntity::class,
            entityColumn = "id",
            parentColumn = "author_id",
            projection = ["name"]
        )
        val authorName: String
    )
}