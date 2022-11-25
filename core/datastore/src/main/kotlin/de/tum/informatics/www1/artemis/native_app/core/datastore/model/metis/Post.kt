package de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis

import androidx.room.ColumnInfo
import androidx.room.Junction
import androidx.room.Relation
import de.tum.informatics.www1.artemis.native_app.core.datastore.room.model.metis.PostingEntity
import de.tum.informatics.www1.artemis.native_app.core.datastore.room.model.metis.StandalonePostTag

data class Post(
    @ColumnInfo(name = "post_id")
    val postId: Int,
    @ColumnInfo(name = "title")
    val title: String?,
    @ColumnInfo(name = "content")
    val content: String,
    @ColumnInfo(name = "author_name")
    val authorName: String,
    @ColumnInfo(name = "author_role")
    val authorRole: PostingEntity.UserRole,
    @Relation(
        entity = StandalonePostTag::class,
        associateBy = Junction()
    )
    val tags: List<String>
)