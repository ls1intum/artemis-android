package de.tum.informatics.www1.artemis.native_app.feature.faq.service.local.impl

import de.tum.informatics.www1.artemis.native_app.core.datastore.room.course.CourseDatabaseProvider
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.Faq
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.FaqCategory
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.mappers.toFaq
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.mappers.toFaqCategoryEntity
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.mappers.toFaqEntity
import de.tum.informatics.www1.artemis.native_app.feature.faq.service.local.FaqDatabaseProvider
import de.tum.informatics.www1.artemis.native_app.feature.faq.service.local.FaqStorageService
import de.tum.informatics.www1.artemis.native_app.feature.faq.service.local.data.FaqCategoryEntity
import de.tum.informatics.www1.artemis.native_app.feature.faq.service.local.data.FaqToFaqCategoryCrossRef

class FaqStorageServiceImpl(
    faqDatabaseProvider: FaqDatabaseProvider,
    courseDatabaseProvider: CourseDatabaseProvider,
) : FaqStorageService {

    private val faqDao = faqDatabaseProvider.faqDao
    private val courseDao = courseDatabaseProvider.courseDao

    override suspend fun store(faq: Faq, courseId: Long, serverUrl: String) {
        val courseClientSideId = courseDao.getOrCreateClientSideId(serverUrl, courseId)

        val existingFaqEntity = faqDao.getById(courseClientSideId, faq.id)?.faq
        val newFaqEntity = if (existingFaqEntity == null) {
            faqDao.insert(faq.toFaqEntity(courseClientSideId))
        } else {
            faqDao.update(faq.toFaqEntity(courseClientSideId).copy(
                clientSideId = existingFaqEntity.clientSideId
            ))
        }

        storeCategories(faq, newFaqEntity.clientSideId, courseClientSideId)
    }

    private suspend fun storeCategories(
        faq: Faq,
        faqClientSideId: Long,
        courseClientSideId: Long
    ) {
        for (category in faq.categories) {
            val categoryEntity = storeCategory(category, courseClientSideId)
            faqDao.upsertCrossRef(
                FaqToFaqCategoryCrossRef(faqClientSideId, categoryEntity.clientSideId)
            )
        }
    }

    private suspend fun storeCategory(
        category: FaqCategory,
        courseClientSideId: Long
    ): FaqCategoryEntity {
        val existingCategory = faqDao.getCategoryByName(courseClientSideId, category.name)
        return if (existingCategory == null) {
            faqDao.insertCategory(category.toFaqCategoryEntity(courseClientSideId))
        } else {
            faqDao.updateCategory(category.toFaqCategoryEntity(courseClientSideId).copy(
                clientSideId = existingCategory.clientSideId
            ))
        }
    }

    override suspend fun getAll(courseId: Long, serverUrl: String): List<Faq> {
        val courseClientSideId = courseDao.getOrCreateClientSideId(serverUrl, courseId)
        return faqDao.getAll(courseClientSideId).map { it.toFaq() }
    }

    override suspend fun getById(faqId: Long, courseId: Long, serverUrl: String): Faq? {
        val courseClientSideId = courseDao.getOrCreateClientSideId(serverUrl, courseId)
        return faqDao.getById(courseClientSideId, faqId)?.toFaq()
    }
}