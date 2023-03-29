package de.tum.informatics.www1.artemis.native_app.android.db

import android.content.Context
import androidx.room.Room
import de.tum.informatics.www1.artemis.native_app.core.datastore.room.BaseDatabaseProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisDatabaseProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.dao.MetisDao
import de.tum.informatics.www1.artemis.native_app.feature.push.PushCommunicationDatabaseProvider
import de.tum.informatics.www1.artemis.native_app.feature.push.communication_notification_model.PushCommunicationDao

internal class DatabaseProvider(context: Context) : BaseDatabaseProvider {
    override val database: AppDatabase =
        Room
            .databaseBuilder(
                context,
                AppDatabase::class.java,
                "artemis_db"
            )
            .fallbackToDestructiveMigration()
            .build()
}

internal class MetisDatabaseProviderImpl(private val databaseProvider: DatabaseProvider) :
    MetisDatabaseProvider, BaseDatabaseProvider by databaseProvider {
    override val metisDao: MetisDao
        get() = databaseProvider.database.metisDao()
    }

internal class PushCommunicationDatabaseProviderImpl(private val databaseProvider: DatabaseProvider) :
    PushCommunicationDatabaseProvider, BaseDatabaseProvider by databaseProvider {
    override val pushCommunicationDao: PushCommunicationDao
        get() = databaseProvider.database.pushCommunicationDao()
}
