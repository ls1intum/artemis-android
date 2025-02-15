package de.tum.informatics.www1.artemis.native_app.android.db

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
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.MetisDao
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.AnswerPostingEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.BasePostingEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.MetisPostContextEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.MetisUserEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.PostReactionEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.StandalonePostTagEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.StandalonePostingEntity
import de.tum.informatics.www1.artemis.native_app.feature.push.communication_notification_model.CommunicationMessageEntity
import de.tum.informatics.www1.artemis.native_app.feature.push.communication_notification_model.PushCommunicationDao
import de.tum.informatics.www1.artemis.native_app.feature.push.communication_notification_model.PushCommunicationEntity

@Database(
    entities = [
        MetisUserEntity::class,
        BasePostingEntity::class,
        AnswerPostingEntity::class,
        StandalonePostingEntity::class,
        StandalonePostTagEntity::class,
        PostReactionEntity::class,
        MetisPostContextEntity::class,
        PushCommunicationEntity::class,
        CommunicationMessageEntity::class,
        CourseEntity::class,
        FaqEntity::class,
        FaqCategoryEntity::class,
        FaqToFaqCategoryCrossRef::class,
    ],
    exportSchema = true,
    version = 16,           // Latest update: Offline FAQs
)
@TypeConverters(RoomTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun metisDao(): MetisDao

    abstract fun pushCommunicationDao(): PushCommunicationDao

    abstract fun courseDao(): CourseDao

    abstract fun faqDao(): FaqDao
}