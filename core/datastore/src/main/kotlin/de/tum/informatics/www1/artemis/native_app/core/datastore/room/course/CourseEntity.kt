package de.tum.informatics.www1.artemis.native_app.core.datastore.room.course

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "course",
    indices = [Index(value = ["server_url", "id"], unique = true)]
)
class CourseEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "client_side_id")
    val clientSideId: Long = 0,
    @ColumnInfo(name = "server_url")
    val serverUrl: String,
    @ColumnInfo(name = "id")
    val id: Long
)