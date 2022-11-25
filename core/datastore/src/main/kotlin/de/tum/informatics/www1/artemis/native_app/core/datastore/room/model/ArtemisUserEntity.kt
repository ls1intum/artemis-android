package de.tum.informatics.www1.artemis.native_app.core.datastore.room.model

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "users", primaryKeys = ["server_id", "id"])
data class ArtemisUserEntity(
    @ColumnInfo(name = "server_id")
    val serverId: String,
    @ColumnInfo(name = "username")
    val username: String,
    @ColumnInfo(name = "id")
    val id: Int
)