package de.tum.informatics.www1.artemis.native_app.core.datastore.room.model.metis

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

/**
 * Defines the relation between the client-side unique post id and server side context.
 * The only reason this class exists is a restriction by the [androidx.room.Relation] interface, where only one primary key
 * is allowed to reference a relation. However, the primary key would consist of (server_id, course_id, exercise_id, lecture_id, post_id).
 * However, we need only one primary key, therefore a uuid is instead used.
 */
@Entity(
    tableName = "metis_post_context",
    primaryKeys = ["server_id", "server_post_id"],
    indices = [Index("client_post_id", unique = true)]
)
data class MetisPostContextEntity(
    @ColumnInfo(name = "server_id")
    val serverId: String,
    @ColumnInfo(name = "course_id")
    val courseId: Int,
    @ColumnInfo(name = "exercise_id")
    val exerciseId: Int,
    @ColumnInfo(name = "lecture_id")
    val lectureId: Int,
    @ColumnInfo(name = "server_post_id")
    val serverPostId: Int,
    @ColumnInfo(name = "client_post_id")
    val clientPostId: String
)