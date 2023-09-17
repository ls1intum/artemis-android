package de.tum.informatics.www1.artemis.native_app.feature.metistest

import android.content.Context
import androidx.room.Room
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.MetisDatabaseProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.dao.MetisDao

/**
 * Database provider for metis unit tests
 */
class MetisDatabaseProviderMock(context: Context) :
    de.tum.informatics.www1.artemis.native_app.feature.metis.shared.MetisDatabaseProvider {

    override val database: MetisTestDatabase = Room
        .databaseBuilder(
            context,
            MetisTestDatabase::class.java,
            "metis_db"
        )
        .fallbackToDestructiveMigration()
        .build()

    override val metisDao: de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.dao.MetisDao get() = database.metisDao()
}
