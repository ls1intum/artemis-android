package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Defines the relation between the client-side unique post id and server side context.
 * This class exists to support a fully offline applications, where posts can be created even when no internet connection is available.
 * For this, a client id is utilized which allows the creation of posts even though no server side id is known yet.
 */
@Entity(
    tableName = "metis_post_context",
    primaryKeys = ["client_post_id", "server_post_id", "course_id", "exercise_id", "lecture_id", "type"],
    foreignKeys = [
        ForeignKey(
            entity = BasePostingEntity::class,
            parentColumns = ["id"],
            childColumns = ["client_post_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("server_post_id", unique = false)]
)
data class MetisPostContextEntity(
    @ColumnInfo(name = "server_id")
    val serverId: String,
    @ColumnInfo(name = "course_id")
    val courseId: Long,
    @ColumnInfo(name = "exercise_id")
    val exerciseId: Long,
    @ColumnInfo(name = "lecture_id")
    val lectureId: Long,
    @ColumnInfo(name = "server_post_id") // a standalone post and a reply may have the same id
    val serverPostId: Long,
    @ColumnInfo(name = "client_post_id")
    val clientPostId: String,
    @ColumnInfo(name = "type")
    val postingType: BasePostingEntity.PostingType
)