package de.tum.informatics.www1.artemis.native_app.feature.faq_test

import android.content.Context
import androidx.room.Room
import de.tum.informatics.www1.artemis.native_app.core.datastore.room.course.CourseDatabaseProvider
import de.tum.informatics.www1.artemis.native_app.feature.faq.service.local.FaqDao
import de.tum.informatics.www1.artemis.native_app.feature.faq.service.local.FaqDatabaseProvider

class FaqDatabaseProviderMock(context: Context): FaqDatabaseProvider, CourseDatabaseProvider {

        override val database: FaqTestDatabase = Room
            .databaseBuilder(
                context,
                FaqTestDatabase::class.java,
                "faq_db"
            )
            .fallbackToDestructiveMigration()
            .build()

    override val faqDao: FaqDao = database.faqDao()
    override val courseDao = database.courseDao()

}