package de.tum.informatics.www1.artemis.native_app.core.datastore.room.model

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "user_roles",
    primaryKeys = ["server_id", "user_id"]
)
class ArtemisUserRoleEntity(
    @ColumnInfo(name = "server_id")
    val serverId: String,
    @ColumnInfo(name = "user_id")
    val userId: Int,
    @ColumnInfo(name = "role")
    val role: UserRole
) {
    enum class UserRole {
        @ColumnInfo(name = "instructor")
        INSTRUCTOR,

        @ColumnInfo(name = "tutor")
        TUTOR,

        @ColumnInfo(name = "user")
        USER
    }
}