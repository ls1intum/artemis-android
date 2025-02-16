package de.tum.informatics.www1.artemis.native_app.feature.faq.service.local

import de.tum.informatics.www1.artemis.native_app.core.datastore.room.BaseDatabaseProvider

interface FaqDatabaseProvider : BaseDatabaseProvider {
    val faqDao: FaqDao
}