package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "users",
    primaryKeys = ["server_id", "id"]
)
class MetisUserEntity(
    @ColumnInfo(name = "server_id")
    val serverId: String,
    @ColumnInfo(name = "id")
    val id: Long,
    @ColumnInfo(name = "name")
    val displayName: String
)