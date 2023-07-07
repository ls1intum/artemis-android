package de.tum.informatics.www1.artemis.native_app.feature.metis.test

import android.content.Context
import androidx.room.Room
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisDatabaseProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.dao.MetisDao

/**
 * Database provider for metis unit tests
 */
class MetisDatabaseProviderMock(context: Context) : MetisDatabaseProvider {

    override val database: MetisTestDatabase = Room
        .databaseBuilder(
            context,
            MetisTestDatabase::class.java,
            "metis_db"
        )
        .fallbackToDestructiveMigration()
        .build()

    override val metisDao: MetisDao get() = database.metisDao()
}
