package de.tum.informatics.www1.artemis.native_app.feature.metistest

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.tum.informatics.www1.artemis.native_app.core.datastore.room.RoomTypeConverters
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.dao.MetisDao
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.db.entities.AnswerPostingEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.db.entities.BasePostingEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.db.entities.MetisPostContextEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.db.entities.MetisUserEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.db.entities.PostReactionEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.db.entities.StandalonePostTagEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.db.entities.StandalonePostingEntity

@Database(
    entities = [
        de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.db.entities.MetisUserEntity::class,
        de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.db.entities.BasePostingEntity::class,
        de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.db.entities.AnswerPostingEntity::class,
        de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.db.entities.StandalonePostingEntity::class,
        de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.db.entities.StandalonePostTagEntity::class,
        de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.db.entities.PostReactionEntity::class,
        de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.db.entities.MetisPostContextEntity::class
    ],
    exportSchema = false,
    version = 1
)
@TypeConverters(RoomTypeConverters::class)
abstract class MetisTestDatabase : RoomDatabase() {
    abstract fun metisDao(): de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.dao.MetisDao
}
