package de.tum.informatics.www1.artemis.native_app.android.db

import android.content.Context
import androidx.room.Room
import de.tum.informatics.www1.artemis.native_app.core.datastore.room.BaseDatabaseProvider
import de.tum.informatics.www1.artemis.native_app.core.datastore.room.course.CourseDao
import de.tum.informatics.www1.artemis.native_app.core.datastore.room.course.CourseDatabaseProvider
import de.tum.informatics.www1.artemis.native_app.feature.faq.service.local.FaqDao
import de.tum.informatics.www1.artemis.native_app.feature.faq.service.local.FaqDatabaseProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.MetisDatabaseProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.MetisDao
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

internal class CourseDatabaseProviderImpl(private val databaseProvider: DatabaseProvider) :
    CourseDatabaseProvider, BaseDatabaseProvider by databaseProvider {
    override val courseDao: CourseDao
        get() = databaseProvider.database.courseDao()
}

internal class FaqDatabaseProviderImpl(private val databaseProvider: DatabaseProvider) :
    FaqDatabaseProvider, BaseDatabaseProvider by databaseProvider {
    override val faqDao: FaqDao
        get() = databaseProvider.database.faqDao()
}
