package de.tum.informatics.www1.artemis.native_app.feature.metistest

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.tum.informatics.www1.artemis.native_app.core.datastore.room.RoomTypeConverters
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.MetisDao
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.AnswerPostingEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.BasePostingEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.MetisPostContextEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.MetisUserEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.PostReactionEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.StandalonePostTagEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.StandalonePostingEntity

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
abstract class MetisTestDatabase : RoomDatabase() {
    abstract fun metisDao(): MetisDao
}
