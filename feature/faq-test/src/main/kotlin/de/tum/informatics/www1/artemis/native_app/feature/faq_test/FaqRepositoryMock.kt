package de.tum.informatics.www1.artemis.native_app.feature.faq_test

import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.FaqRepository
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.Faq
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FaqRepositoryMock(
    var faqs: List<Faq> = emptyList()
) : FaqRepository {

    override suspend fun getFaqs(courseId: Long): Flow<DataState<List<Faq>>> {
        return flowOf(DataState.Success(faqs))
    }

    override suspend fun getFaq(courseId: Long, faqId: Long): Flow<DataState<Faq>> {
        val faq: Faq? = faqs.firstOrNull { it.id == faqId }
        val dataState = if (faq != null) {
            DataState.Success(faq)
        } else {
            DataState.Failure(Exception("Faq not found"))
        }
        return flowOf(dataState)
    }
}