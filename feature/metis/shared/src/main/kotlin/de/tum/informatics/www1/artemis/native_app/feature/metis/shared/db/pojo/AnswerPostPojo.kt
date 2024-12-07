package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo

import androidx.room.ColumnInfo
import androidx.room.Ignore
import androidx.room.Relation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IAnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.UserRole
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.BasePostingEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.MetisPostContextEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.MetisUserEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.PostReactionEntity
import kotlinx.datetime.Instant

data class AnswerPostPojo(
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
        projection = ["author_id", "creation_date", "updated_date", "content", "author_role"]
    )
    private val basePostingCache: BasePostingCache,
    @Relation(
        entity = PostReactionEntity::class,
        entityColumn = "post_id",
        parentColumn = "post_id",
        projection = ["author_id", "emoji", "id"]
    )
    override val reactions: List<PostPojo.Reaction>,
    @Relation(
        entity = MetisPostContextEntity::class,
        entityColumn = "client_post_id",
        parentColumn = "post_id",
        projection = ["client_post_id", "server_post_id"]
    )
    val serverPostIdCache: ServerPostIdCache
) : IAnswerPost {
    @Ignore
    override val creationDate: Instant = basePostingCache.creationDate

    @Ignore
    override val updatedDate: Instant? = basePostingCache.updatedDate

    @Ignore
    override val content: String? = basePostingCache.content

    @Ignore
    override val authorRole: UserRole = basePostingCache.authorRole

    @Ignore
    override val authorName: String = basePostingCache.authorName

    @Ignore
    override val authorId: Long = basePostingCache.authorId

    @Ignore
    override val authorImageUrl: String? = basePostingCache.authorImageUrl

    @Ignore
    override val serverPostId: Long? = serverPostIdCache.serverPostId

    @Ignore
    override val clientPostId: String = postId

    data class BasePostingCache(
        @ColumnInfo(name = "id")
        val serverPostId: Long,
        @ColumnInfo(name = "author_id")
        val authorId: Long,
        @ColumnInfo(name = "creation_date")
        val creationDate: Instant,
        @ColumnInfo(name = "updated_date")
        val updatedDate: Instant?,
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
        val authorName: String,
        @Relation(
            entity = MetisUserEntity::class,
            entityColumn = "id",
            parentColumn = "author_id",
            projection = ["image_url"]
        )
        val authorImageUrl: String?
    )

    data class ServerPostIdCache(
        @ColumnInfo(name = "server_post_id")
        val serverPostId: Long?
    )
}