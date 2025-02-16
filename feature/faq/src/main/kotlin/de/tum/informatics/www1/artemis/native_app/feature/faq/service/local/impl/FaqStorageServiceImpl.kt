package de.tum.informatics.www1.artemis.native_app.feature.faq.service.local.impl

import androidx.room.withTransaction
import de.tum.informatics.www1.artemis.native_app.core.datastore.room.course.CourseDatabaseProvider
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.Faq
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.FaqCategory
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.mappers.toFaq
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.mappers.toFaqCategoryEntity
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.mappers.toFaqEntity
import de.tum.informatics.www1.artemis.native_app.feature.faq.service.local.FaqDatabaseProvider
import de.tum.informatics.www1.artemis.native_app.feature.faq.service.local.FaqStorageService
import de.tum.informatics.www1.artemis.native_app.feature.faq.service.local.data.FaqToFaqCategoryCrossRef

class FaqStorageServiceImpl(
    faqDatabaseProvider: FaqDatabaseProvider,
    courseDatabaseProvider: CourseDatabaseProvider,
) : FaqStorageService {

    private val faqDao = faqDatabaseProvider.faqDao
    private val courseDao = courseDatabaseProvider.courseDao
    private val database = faqDatabaseProvider.database         // Is the same as database from courseDatabaseProvider

    override suspend fun store(faq: Faq, courseId: Long, serverUrl: String) {
        database.withTransaction {
            val courseLocalId = courseDao.getOrCreateLocalId(serverUrl, courseId)
            val existingFaqEntity = faqDao.getById(courseLocalId, faq.id)?.faq

            val faqEntityLocalId: Long
            if (existingFaqEntity == null) {
                faqEntityLocalId = faqDao.insert(faq.toFaqEntity(courseLocalId))
            } else {
                faqEntityLocalId = existingFaqEntity.localId
                faqDao.update(
                    faq.toFaqEntity(courseLocalId).copy(
                        localId = existingFaqEntity.localId
                    )
                )
            }

            storeCategories(faq, faqEntityLocalId, courseLocalId)
        }
    }

    private suspend fun storeCategories(
        faq: Faq,
        faqLocalId: Long,
        courseLocalId: Long
    ) {
        for (category in faq.categories) {
            val categoryEntityLocalId = storeCategory(category, courseLocalId)
            faqDao.upsertCrossRef(
                FaqToFaqCategoryCrossRef(faqLocalId, categoryEntityLocalId)
            )
        }
    }

    private suspend fun storeCategory(
        category: FaqCategory,
        courseLocalId: Long
    ): Long {
        val existingCategory = faqDao.getCategoryByName(courseLocalId, category.name)
        return if (existingCategory == null) {
            faqDao.insertCategory(category.toFaqCategoryEntity(courseLocalId))
        } else {
            faqDao.updateCategory(category.toFaqCategoryEntity(courseLocalId).copy(
                localId = existingCategory.localId
            ))
            existingCategory.localId
        }
    }

    override suspend fun getAll(courseId: Long, serverUrl: String): List<Faq> {
        return database.withTransaction {
            val courseLocalId = courseDao.getOrCreateLocalId(serverUrl, courseId)
            return@withTransaction faqDao.getAll(courseLocalId).map { it.toFaq() }
        }
    }

    override suspend fun getById(faqId: Long, courseId: Long, serverUrl: String): Faq? {
        return database.withTransaction {
            val courseLocalId = courseDao.getOrCreateLocalId(serverUrl, courseId)
            return@withTransaction faqDao.getById(courseLocalId, faqId)?.toFaq()
        }
    }
}