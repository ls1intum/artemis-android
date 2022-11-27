package de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis

import androidx.room.ColumnInfo
import androidx.room.Relation
import de.tum.informatics.www1.artemis.native_app.core.datastore.room.model.metis.AnswerPostingEntity
import de.tum.informatics.www1.artemis.native_app.core.datastore.room.model.metis.PostReactionEntity
import de.tum.informatics.www1.artemis.native_app.core.datastore.room.model.metis.BasePostingEntity
import de.tum.informatics.www1.artemis.native_app.core.datastore.room.model.metis.StandalonePostTagEntity

class Post(
    @ColumnInfo(name = "client_post_id")
    val clientPostId: String,
    @ColumnInfo(name = "server_post_id")
    val serverPostId: Int,
    @ColumnInfo(name = "title")
    val title: String?,
    @ColumnInfo(name = "content")
    val content: String,
    @ColumnInfo(name = "author_name")
    val authorName: String,
    @ColumnInfo(name = "author_role")
    val authorRole: BasePostingEntity.UserRole,
    @Relation(
        entity = StandalonePostTagEntity::class,
        parentColumn = "post_id",
        entityColumn = "client_post_id",
        projection = ["tag"]
    )
    val tags: List<String>,
    @Relation(
        entity = AnswerPostingEntity::class,
        parentColumn = "parent_post_id",
        entityColumn = "client_post_id"
    )
    val answerPostings: List<AnswerPost>,
    @Relation(
        entity = PostReactionEntity::class,
        entityColumn = "post_id",
        parentColumn = "client_post_id"
    )
    val reactions: List<PostReactionEntity>
)