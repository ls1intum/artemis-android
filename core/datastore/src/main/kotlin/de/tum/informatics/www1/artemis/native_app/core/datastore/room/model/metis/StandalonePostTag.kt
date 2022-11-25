package de.tum.informatics.www1.artemis.native_app.core.datastore.room.model.metis

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "post_tags",
    primaryKeys = ["server_id", "post_id"],
    foreignKeys = [
        ForeignKey(
            entity = PostingEntity::class,
            parentColumns = ["server_id", "id"],
            childColumns = ["server_id", "post_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class StandalonePostTag(
    @ColumnInfo(name = "server_id")
    val serverId: String,
    @ColumnInfo(name = "post_id")
    val postId: Int,
    @ColumnInfo(name = "tag")
    val tag: String
)