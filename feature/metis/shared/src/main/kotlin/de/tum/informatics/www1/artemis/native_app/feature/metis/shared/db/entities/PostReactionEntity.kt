package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "reactions",
    primaryKeys = ["post_id", "emoji", "author_id", "server_id"],
    indices = [Index("server_id", "author_id", name = "server_id_author_id_index")]
)
data class PostReactionEntity(
    @ColumnInfo(name = "server_id")
    val serverId: String,
    @ColumnInfo(name = "post_id")
    val postId: String,
    @ColumnInfo(name = "emoji")
    val emojiId: String,
    @ColumnInfo(name = "author_id")
    val authorId: Long,
    @ColumnInfo(name = "id")
    val id: Long
)