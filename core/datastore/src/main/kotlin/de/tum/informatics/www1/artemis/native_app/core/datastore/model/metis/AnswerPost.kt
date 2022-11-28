package de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis

import androidx.room.ColumnInfo
import androidx.room.Relation
import de.tum.informatics.www1.artemis.native_app.core.datastore.room.model.metis.BasePostingEntity

data class AnswerPost(
    @ColumnInfo(name = "parent_post_id")
    val parentPostId: String,
    @ColumnInfo(name = "post_id")
    val postId: String,
    @ColumnInfo(name = "resolves_post")
    val resolvesPost: Boolean,
    @Relation(
        entity = BasePostingEntity::class,
        entityColumn = "id",
        parentColumn = "post_id"
    )
    val basePosting: BasePostingEntity
)