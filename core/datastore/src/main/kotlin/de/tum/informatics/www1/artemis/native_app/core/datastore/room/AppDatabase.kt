package de.tum.informatics.www1.artemis.native_app.core.datastore.room

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.tum.informatics.www1.artemis.native_app.core.datastore.dao.MetisDao
import de.tum.informatics.www1.artemis.native_app.core.datastore.room.model.metis.*

@Database(
    entities = [
        MetisUserEntity::class,
        PostingEntity::class,
        AnswerPosting::class,
        StandalonePosting::class,
        StandalonePostTag::class,
        PostReaction::class
    ],
    exportSchema = true,
    version = 1
)
@TypeConverters(RoomTypeConverters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun metisDao(): MetisDao
}