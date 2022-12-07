package de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis

import androidx.room.ColumnInfo
import androidx.room.Ignore
import androidx.room.Relation
import de.tum.informatics.www1.artemis.native_app.core.datastore.room.model.metis.BasePostingEntity
import de.tum.informatics.www1.artemis.native_app.core.datastore.room.model.metis.MetisPostContextEntity
import de.tum.informatics.www1.artemis.native_app.core.datastore.room.model.metis.MetisUserEntity
import de.tum.informatics.www1.artemis.native_app.core.datastore.room.model.metis.PostReactionEntity
import kotlinx.datetime.Instant

data class AnswerPost constructor(
    @ColumnInfo(name = "parent_post_id")
    val parentPostId: String,
    @ColumnInfo(name = "post_id")
    val postId: String,
    @ColumnInfo(name = "resolves_post")
    val resolvesPost: Boolean,
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
    val reactions: List<Post.Reaction>
) {
    @Ignore
    val creationDate: Instant = basePostingCache.creationDate
    @Ignore
    val content: String? = basePostingCache.content
    @Ignore
    val authorRole: BasePostingEntity.UserRole = basePostingCache.authorRole
    @Ignore
    val authorName: String = basePostingCache.authorName

    data class BasePostingCache(
        @ColumnInfo(name = "author_id")
        val authorId: Int,
        @ColumnInfo(name = "creation_date")
        val creationDate: Instant,
        @ColumnInfo(name = "content")
        val content: String?,
        @ColumnInfo(name = "author_role")
        val authorRole: BasePostingEntity.UserRole,
        @Relation(
            entity = MetisUserEntity::class,
            entityColumn = "id",
            parentColumn = "author_id",
            projection = ["name"]
        )
        val authorName: String
    )
}