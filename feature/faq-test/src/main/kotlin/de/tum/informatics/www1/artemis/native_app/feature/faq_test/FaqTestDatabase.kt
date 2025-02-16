package de.tum.informatics.www1.artemis.native_app.feature.faq_test

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.tum.informatics.www1.artemis.native_app.core.datastore.room.RoomTypeConverters
import de.tum.informatics.www1.artemis.native_app.core.datastore.room.course.CourseDao
import de.tum.informatics.www1.artemis.native_app.core.datastore.room.course.CourseEntity
import de.tum.informatics.www1.artemis.native_app.feature.faq.service.local.FaqDao
import de.tum.informatics.www1.artemis.native_app.feature.faq.service.local.data.FaqCategoryEntity
import de.tum.informatics.www1.artemis.native_app.feature.faq.service.local.data.FaqEntity
import de.tum.informatics.www1.artemis.native_app.feature.faq.service.local.data.FaqToFaqCategoryCrossRef

@Database(
    entities = [
        FaqEntity::class,
        FaqCategoryEntity::class,
        FaqToFaqCategoryCrossRef::class,
        CourseEntity::class
    ],
    exportSchema = false,
    version = 1
)
@TypeConverters(RoomTypeConverters::class)
abstract class FaqTestDatabase : RoomDatabase() {
    abstract fun faqDao(): FaqDao
    abstract fun courseDao(): CourseDao
}