package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo

import androidx.room.ColumnInfo
import androidx.room.Ignore
import androidx.room.Relation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.StandalonePostId
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.DisplayPriority
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IReaction
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IStandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.UserRole
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.AnswerPostingEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.BasePostingEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.MetisUserEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.PostReactionEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.StandalonePostTagEntity
import kotlinx.datetime.Instant

data class PostPojo(
    @ColumnInfo(name = "client_post_id")
    override val clientPostId: String,
    @ColumnInfo(name = "server_post_id")
    override val serverPostId: Long?,
    @ColumnInfo(name = "title")
    override val title: String?,
    @ColumnInfo(name = "content")
    override val content: String,
    @ColumnInfo(name = "author_name")
    override val authorName: String,
    @ColumnInfo(name = "author_role")
    override val authorRole: UserRole,
    @ColumnInfo(name = "author_id")
    override val authorId: Long,
    @ColumnInfo(name = "author_image_url")
    override val authorImageUrl: String?,
    @ColumnInfo(name = "creation_date")
    override val creationDate: Instant,
    @ColumnInfo(name = "updated_date")
    override val updatedDate: Instant?,
    @ColumnInfo(name = "resolved")
    override val resolved: Boolean,
    @ColumnInfo(name = "is_saved")
    override val isSaved: Boolean,
    @ColumnInfo(name = "context")
    val courseWideContext: BasePostingEntity.CourseWideContext?,
    @ColumnInfo(name = "display_priority")
    override val displayPriority: DisplayPriority?,
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
    override val answers: List<AnswerPostPojo>,
    @Relation(
        entity = PostReactionEntity::class,
        entityColumn = "post_id",
        parentColumn = "client_post_id",
        projection = ["author_id", "emoji", "id"]
    )
    override val reactions: List<Reaction>
) : IStandalonePost {

    @Ignore
    override val key: Any = clientPostId

    @Ignore
    val orderedAnswerPostings = answers.sortedBy { it.creationDate }

    @Ignore
    override val standalonePostId: StandalonePostId = StandalonePostId.ClientSideId(clientPostId)

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
        override val id: Long,
        @ColumnInfo(name = "creation_date")
        override val creationDate: Instant?
    ) : IReaction {
        @Ignore
        override val creatorId: Long = authorId
    }
}