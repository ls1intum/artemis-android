package de.tum.informatics.www1.artemis.native_app.feature.metis.model.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "answer_postings",
    primaryKeys = ["post_id"],
    foreignKeys = [
        //The foreign key for its own entry in the posting table.
        ForeignKey(
            entity = BasePostingEntity::class,
            parentColumns = ["id"],
            childColumns = ["post_id"],
            onDelete = ForeignKey.CASCADE
        ),
        //The foreign key to the post that this post is an answer to
        ForeignKey(
            entity = BasePostingEntity::class,
            parentColumns = ["id"],
            childColumns = ["parent_post_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("parent_post_id", name = "ppi_index")]
)
data class AnswerPostingEntity(
    @ColumnInfo(name = "post_id")
    val postId: String,
    @ColumnInfo(name = "parent_post_id")
    val parentPostId: String,
    @ColumnInfo(name = "resolves_post")
    val resolvesPost: Boolean
)