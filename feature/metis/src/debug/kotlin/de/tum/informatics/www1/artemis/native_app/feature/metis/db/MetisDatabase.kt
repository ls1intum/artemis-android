package de.tum.informatics.www1.artemis.native_app.feature.metis.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.tum.informatics.www1.artemis.native_app.core.datastore.room.RoomTypeConverters
import de.tum.informatics.www1.artemis.native_app.feature.metis.dao.MetisDao
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.room.AnswerPostingEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.room.BasePostingEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.room.MetisPostContextEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.room.MetisUserEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.room.PostReactionEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.room.StandalonePostTagEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.room.StandalonePostingEntity

@Database(
    entities = [
        MetisUserEntity::class,
        BasePostingEntity::class,
        AnswerPostingEntity::class,
        StandalonePostingEntity::class,
        StandalonePostTagEntity::class,
        PostReactionEntity::class,
        MetisPostContextEntity::class
    ],
    exportSchema = false,
    version = 1
)
@TypeConverters(RoomTypeConverters::class)
abstract class MetisDatabase : RoomDatabase() {
    abstract fun metisDao(): MetisDao
}