package de.tum.informatics.www1.artemis.native_app.core.datastore.room

import androidx.room.RoomDatabase

interface BaseDatabaseProvider {
    val database: RoomDatabase
}