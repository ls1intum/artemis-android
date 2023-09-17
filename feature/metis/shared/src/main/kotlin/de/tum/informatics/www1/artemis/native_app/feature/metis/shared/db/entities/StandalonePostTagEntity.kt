package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "post_tags",
    primaryKeys = ["post_id", "tag"],
    foreignKeys = [
        ForeignKey(
            entity = BasePostingEntity::class,
            parentColumns = ["id"],
            childColumns = ["post_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class StandalonePostTagEntity(
    @ColumnInfo(name = "post_id")
    val postId: String,
    @ColumnInfo(name = "tag")
    val tag: String
)