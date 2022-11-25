package de.tum.informatics.www1.artemis.native_app.core.datastore.impl

import android.content.Context
import androidx.room.Room
import de.tum.informatics.www1.artemis.native_app.core.datastore.room.AppDatabase

/**
 * Service that provides an instance of the database
 */
class DatabaseProvider(context: Context) {
    val database: AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "artemis_db").build()
}