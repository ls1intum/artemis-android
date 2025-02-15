package de.tum.informatics.www1.artemis.native_app.feature.faq.service.local

import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.Faq

interface FaqStorageService {

    suspend fun store(faq: Faq, courseId: Long, serverUrl: String)

    suspend fun getAll(courseId: Long, serverUrl: String): List<Faq>

    suspend fun getById(faqId: Long, courseId: Long, serverUrl: String): Faq?


    suspend fun storeAll(
        faqs: List<Faq>,
        courseId: Long,
        serverUrl: String,
    ) = faqs.forEach { store(it, courseId, serverUrl) }
}