package de.tum.informatics.www1.artemis.native_app.core.datastore.room

import androidx.room.TypeConverter
import kotlinx.datetime.Instant

class RoomTypeConverters {

    @TypeConverter
    fun fromInstant(value: Instant?): Long? {
        return value?.epochSeconds
    }

    @TypeConverter
    fun toInstant(value: Long?): Instant? {
        return Instant.fromEpochSeconds(value ?: return null)
    }
}